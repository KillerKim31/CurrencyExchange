package servlets;

import Exceptions.DatabaseException;
import Service.CurrencyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Currency;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet("/currency/*")
public class CurrencyServlet extends BaseServlet {

    private CurrencyService currencyService;

    protected final String ERR_MSG_INCORRECT_ENDPOINT = "Не указан код валюты";
    protected final String ERR_MSG_DATABASE           = "Ошибка при взаимодействии с БД.";
    protected final String ERR_MSG_INNER              = "Внутренняя ошибка сервера.";
    protected final String ERR_MSG_CURRENCY_NOT_FOUND = "Указанная валюта не найдена.";

    @Override
    public void init() throws ServletException {
        super.init();
        currencyService = new CurrencyService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        try {

            // получаем код валюты
            String currencyCode = (req.getPathInfo() != null) ? req.getPathInfo().substring(1).toUpperCase() : null;
            if (currencyCode == null || currencyCode.isBlank()) {
                generateError(resp, HttpServletResponse.SC_BAD_REQUEST, mapper, ERR_MSG_INCORRECT_ENDPOINT);
                return;
            }
            // Получает запись из БД
            Currency currencyEntry = currencyService.getCurrencyEntry(currencyCode);
            if (currencyEntry == null) {
                generateError(resp, HttpServletResponse.SC_NOT_FOUND, mapper, ERR_MSG_CURRENCY_NOT_FOUND);
                return;
            }
            // возвращаем объект валюты в JSON
            resp.setStatus(HttpServletResponse.SC_OK);
            mapper.writeValue(resp.getWriter(), currencyEntry);
        } catch (DatabaseException e) {
            generateError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, mapper, ERR_MSG_DATABASE);
        } catch (Exception e) {
            generateError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, mapper, ERR_MSG_INNER);
        }

    }

}
