package servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.Exchange;
import repositories.CrudRepository;
import repositories.ExchangeCrud;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {

    private final String ERR_MSG_INCORRECT_DATA     = "Неверно введены данные. Пример: rate = 1.45";
    private final String ERR_MSG_INCORRECT_ENDPOINT = "Неверно указан либо отсутствует обменный код валюты. Пример обменного кода: RUBEUR";
    private final String ERR_MSG_INNER              = "Внутренняя ошибка сервера.";
    private final String ERR_MSG_DATABASE           = "Ошибка при взаимодействии с БД.";
    private final String ERR_MSG_NOT_FOUND          = "Запись с таким обменным курсом не найдена.";


    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Перехватываем PATCH запросы
        if ("PATCH".equalsIgnoreCase(req.getMethod())) {
            doPatch(req, resp);
            return;
        }
        // остальные методы (GET, POST, PUT, DELETE)
        super.service(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        ObjectMapper mapper = new ObjectMapper();

        try {
            // получаем коды обменного курса
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.length() < 7 || pathInfo.length() > 7) {
                generateError(resp, HttpServletResponse.SC_BAD_REQUEST, mapper, ERR_MSG_INCORRECT_ENDPOINT);
                return;
            }
            var baseCurrencyCode   = pathInfo.substring(1, 4).toUpperCase();
            var targetCurrencyCode = pathInfo.substring(4).toUpperCase();

            // Выполняем поиск записи по обменному курсу
            Exchange exchangeEntry;
            try {
                exchangeEntry = new ExchangeCrud().findByCodes(baseCurrencyCode, targetCurrencyCode);
            }
            catch (Exception e) {
                generateError(resp, HttpServletResponse.SC_NOT_FOUND, mapper, ERR_MSG_DATABASE);
                return;
            }

            // Проверяем нашлись ли записи по обменному курсу
            if (exchangeEntry == null) {
                generateError(resp, HttpServletResponse.SC_NOT_FOUND, mapper, ERR_MSG_NOT_FOUND);
                return;
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            mapper.writeValue(resp.getWriter(), exchangeEntry);

        }
        catch (Exception e) {
            generateError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, mapper, ERR_MSG_INNER);
        }

    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        ObjectMapper mapper = new ObjectMapper();

        try {

            String pathInfo = req.getPathInfo();
            String requestBody = Utils.Utils.getRequestBodyString(req);
            HashMap<String, Object> jsonObject = mapper.readValue(requestBody, HashMap.class);
            Double rate = null;
            String baseCurrencyCode;
            String targetCurrencyCode;

            // Проверка валидации обменного курса и параметров в теле запроса
            if (pathInfo == null || pathInfo.length() < 7 || pathInfo.length() > 7) {
                generateError(resp, HttpServletResponse.SC_BAD_REQUEST, mapper, ERR_MSG_INCORRECT_ENDPOINT);
                return;
            }
            baseCurrencyCode = pathInfo.substring(1, 4).toUpperCase();
            targetCurrencyCode = pathInfo.substring(4).toUpperCase();

            if (jsonObject.get("rate") != null) {
                try {
                    rate = Double.parseDouble(jsonObject.get("rate").toString());
                }
                catch (Exception e) {
                    generateError(resp, HttpServletResponse.SC_BAD_REQUEST, mapper, ERR_MSG_INCORRECT_DATA);
                    return;
                }
            }

            Exchange exchangeEntry;
            try {
                // Выполняем поиск записи по обменному курсу
                ExchangeCrud exchangeCrud = new ExchangeCrud();
                exchangeEntry = exchangeCrud.findByCodes(baseCurrencyCode, targetCurrencyCode);
                if (exchangeEntry == null) {
                    generateError(resp, HttpServletResponse.SC_NOT_FOUND, mapper, ERR_MSG_NOT_FOUND);
                    return;
                }
                // Вносим изменения в данные и применяем к БД
                exchangeEntry.setRate(rate);
                exchangeCrud.update(exchangeEntry);
            }
            catch (Exception e) {
                generateError(resp, HttpServletResponse.SC_NOT_FOUND, mapper, ERR_MSG_DATABASE);
                return;
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            mapper.writeValue(resp.getWriter(), exchangeEntry);

        }
        catch (Exception e) {
            generateError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, mapper, ERR_MSG_INNER);
        }

    }

    private void generateError(HttpServletResponse resp, int ErrorCode, ObjectMapper mapper, String errorText) throws IOException {
        resp.setStatus(ErrorCode);
        Map<String, Object> errorObject = new HashMap<>();
        errorObject.put("error", errorText);
        mapper.writeValue(resp.getWriter(), errorObject);
    }

}
