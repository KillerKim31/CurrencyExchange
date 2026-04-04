package servlets;

import Exceptions.DatabaseException;
import Exceptions.DuplicateEntryException;
import Exceptions.InvalidEntryException;
import Exceptions.NotFoundEntryException;
import Service.ExchangeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Exchange;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends BaseServlet  {

    private ExchangeService entryService;

    protected final String ERR_MSG_INCORRECT_DATA = "Неверно введены данные. Пример: baseCurrencyCode = \"USD\", targetCurrencyCode = \"RUB\", rate = 0.99";
    protected final String ERR_MSG_INNER          = "Внутренняя ошибка сервера.";
    protected final String ERR_DUPLICATE_ETRY     = "Запись с таким обменным курсом уже существует.";
    protected final String ERR_CURRENCY_NOT_FIND  = "Одна или обе валюты из валютной пары не найдены в БД.";
    protected final String ERR_MSG_DATABASE       = "Ошибка при взаимодействии с БД.";

    @Override
    public void init() throws ServletException {
        super.init();
        entryService = new ExchangeService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        try {
            List<Exchange> exchangeList = entryService.getExchangeList();
            resp.setStatus(HttpServletResponse.SC_OK);
            mapper.writeValue(resp.getWriter(), exchangeList);
        } catch (Exception e) {
            generateError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, mapper, ERR_MSG_INNER);
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        ObjectMapper mapper = new ObjectMapper();
        try {
            String pathInfo = req.getPathInfo();
            String requestBody = Utils.Utils.getRequestBodyString(req);
            Long id = entryService.createExchange(pathInfo, requestBody);
            resp.setStatus(HttpServletResponse.SC_OK);
            mapper.writeValue(resp.getWriter(), Map.of("id", id));
        }
        catch (InvalidEntryException | IOException e) {
            generateError(resp, HttpServletResponse.SC_BAD_REQUEST, mapper, ERR_MSG_INCORRECT_DATA);
        } catch (NotFoundEntryException e) {
            generateError(resp, HttpServletResponse.SC_NOT_FOUND, mapper, ERR_CURRENCY_NOT_FIND);
        } catch (DuplicateEntryException e) {
            generateError(resp, HttpServletResponse.SC_CONFLICT, mapper, ERR_DUPLICATE_ETRY);
        } catch (DatabaseException e) {
            generateError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, mapper, ERR_MSG_DATABASE);
        } catch (Exception e) {
            generateError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, mapper, ERR_MSG_INNER);
        }

    }

}
