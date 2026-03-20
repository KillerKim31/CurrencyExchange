package servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.Exchange;
import repositories.ExchangeCrud;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {

    private final String ERR_MSG_INCORRECT_DATA = "Неверно указан либо отсутствует обменный код валюты. Пример обменного кода: RUBEUR";
    private final String ERR_MSG_INNER          = "Внутренняя ошибка сервера.";
    private final String ERR_MSG_DATABASE       = "Ошибка при взаимодействии с БД.";
    private final String ERR_MSG_NOT_FOUND      = "Запись с таким обменным курсом не найдена.";


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        ObjectMapper mapper = new ObjectMapper();

        try {
            // получаем коды обменного курса
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.length() <= 7 || pathInfo.length() > 7) {
                generateError(resp, HttpServletResponse.SC_BAD_REQUEST, mapper, ERR_MSG_INCORRECT_DATA);
                return;
            }

            // Выполняем поиск записи по обменному курсу
            Exchange exchangeEntry;
            try {
                var baseCurrencyCode = pathInfo.substring(1, 4).toUpperCase();
                var targetCurrencyCode = pathInfo.substring(4).toUpperCase();
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

    private void generateError(HttpServletResponse resp, int ErrorCode, ObjectMapper mapper, String errorText) throws IOException {
        resp.setStatus(ErrorCode);
        Map<String, Object> errorObject = new HashMap<>();
        errorObject.put("error", errorText);
        mapper.writeValue(resp.getWriter(), errorObject);
    }

}
