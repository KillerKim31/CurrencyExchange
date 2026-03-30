package servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.Exchange;
import repositories.CurrencyCrud;
import repositories.ExchangeCrud;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet  {

    private final String ERR_MSG_INCORRECT_DATA = "Неверно введены данные. Пример: baseCurrencyCode = \"USD\", targetCurrencyCode = \"RUB\", rate = 0.99";
    private final String ERR_MSG_INNER          = "Внутренняя ошибка сервера.";
    private final String ERR_DUPLICATE_ETRY     = "Запись с таким обменным курсом уже существует.";
    private final String ERR_CURRENCY_NOT_FIND  = "Одна или обе валюты из валютной пары не найдены в БД.";
    private final String ERR_MSG_DATABASE       = "Ошибка при взаимодействии с БД.";


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> errorObject;
        ObjectMapper mapper = new ObjectMapper();

        try {
            List<Exchange> exchangeList = new ExchangeCrud().findAll();
            resp.setStatus(HttpServletResponse.SC_OK);
            mapper.writeValue(resp.getWriter(), exchangeList);
        }
        catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            errorObject = new HashMap<>();
            errorObject.put("error", "База данных недоступна");
            mapper.writeValue(resp.getWriter(), errorObject);
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        ObjectMapper mapper = new ObjectMapper();
        Exchange newEntry = null;
        Map<String, Object> jsonObject;
        var entryCrud = new ExchangeCrud();

        // Проверка валидности полей
        try {
            String requestBody = Utils.Utils.getRequestBodyString(req);
            jsonObject = mapper.readValue(requestBody, HashMap.class);
            if (!Utils.Utils.isValidExchangeJSON(jsonObject)) {
                generateError(resp, HttpServletResponse.SC_BAD_REQUEST, mapper, ERR_MSG_INCORRECT_DATA);
                return;
            }
        }
        catch (IOException e) {
            generateError(resp, HttpServletResponse.SC_BAD_REQUEST, mapper, ERR_MSG_INCORRECT_DATA);
            return;
        }

        String baseCurrencyCode   = jsonObject.get("baseCurrencyCode").toString();
        String targetCurrencyCode = jsonObject.get("targetCurrencyCode").toString();
        Double rate               = Double.parseDouble(jsonObject.get("rate").toString());

        // Проверка на дубликат
        if (entryCrud.findByCodes(baseCurrencyCode, targetCurrencyCode) != null) {
            generateError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, mapper, ERR_DUPLICATE_ETRY);
            return;
        }

        // Создание нового объекта для сохранения в БД
        try {
            // Проверка существования указанных курсов валют
            var currencyCrud   = new CurrencyCrud();
            var baseCurrency   = currencyCrud.findByCode(baseCurrencyCode);
            var targetCurrency = currencyCrud.findByCode(targetCurrencyCode);

            if (baseCurrency == null || targetCurrency == null) {
                generateError(resp, HttpServletResponse.SC_NOT_FOUND, mapper, ERR_CURRENCY_NOT_FIND);
                return;
            }

            newEntry = new Exchange(
                    baseCurrency,
                    targetCurrency,
                    rate
            );
        }
        catch (Exception e) {
            generateError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, mapper, ERR_MSG_INNER);
            return;
        }

        // Сохранение в БД
        try {
            Long id = entryCrud.save(newEntry);
            if (id == null || id <= 0) {
                generateError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, mapper, ERR_MSG_DATABASE);
                return;
            }
            // Успешное создание записи
            resp.setStatus(HttpServletResponse.SC_CREATED);
            mapper.writeValue(resp.getWriter(), Map.of("id", id));
        } catch (Exception e) {
            generateError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, mapper, ERR_MSG_DATABASE);
            return;
        }

    }

    private void generateError(HttpServletResponse resp, int ErrorCode, ObjectMapper mapper, String errorText) throws IOException {
        resp.setStatus(ErrorCode);
        Map<String, Object> errorObject = new HashMap<>();
        errorObject.put("error", errorText);
        mapper.writeValue(resp.getWriter(), errorObject);
    }

}
