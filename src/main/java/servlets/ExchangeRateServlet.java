package servlets;

import Exceptions.DatabaseException;
import Exceptions.InvalidEntryException;
import Exceptions.NotFoundEntryException;
import Service.ExchangeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Exchange;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends BaseServlet {

    private ExchangeService entryService;

    protected final String ERR_MSG_INCORRECT_DATA        = """
                                                                Неверно указан либо отсутствует обменный код валюты.
                                                                Пример обменного кода: RUBEUR
                                                            """;
    protected final String ERR_MSG_INCORRECT_ENDPOINT    = "Неверно введены данные. Пример: rate = 1.45";
    protected final String ERR_MSG_INNER                 = "Внутренняя ошибка сервера.";
    protected final String ERR_MSG_DATABASE              = "Ошибка при взаимодействии с БД.";
    protected final String ERR_MSG_NOT_FOUND             = "Запись с таким обменным курсом не найдена.";

    @Override
    public void init() throws ServletException {
        super.init();
        entryService = new ExchangeService();
    }

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

        ObjectMapper mapper = new ObjectMapper();
        try {
            String pathInfo = req.getPathInfo();
            Exchange exchangeEntry = entryService.getExchange(pathInfo);
            resp.setStatus(HttpServletResponse.SC_OK);
            mapper.writeValue(resp.getWriter(), exchangeEntry);
        }
        catch (InvalidEntryException | IOException e) {
            generateError(resp, HttpServletResponse.SC_BAD_REQUEST, mapper, ERR_MSG_INCORRECT_DATA);
        } catch (NotFoundEntryException e) {
            generateError(resp, HttpServletResponse.SC_NOT_FOUND, mapper, ERR_MSG_NOT_FOUND);
        } catch (Exception e) {
            generateError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, mapper, ERR_MSG_INNER);
        }

    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        try {
            String pathInfo = req.getPathInfo();
            String requestBody = Utils.Utils.getRequestBodyString(req);
            Exchange exchangeEntry = entryService.updateExchangeRate(pathInfo, requestBody);
            resp.setStatus(HttpServletResponse.SC_OK);
            mapper.writeValue(resp.getWriter(), exchangeEntry);
        }
        catch (InvalidEntryException | IOException e) {
            generateError(resp, HttpServletResponse.SC_BAD_REQUEST, mapper, ERR_MSG_INCORRECT_DATA + "\n" + ERR_MSG_INCORRECT_ENDPOINT);
        } catch (NotFoundEntryException e) {
            generateError(resp, HttpServletResponse.SC_NOT_FOUND, mapper, ERR_MSG_NOT_FOUND);
        } catch (Exception e) {
            generateError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, mapper, ERR_MSG_INNER);
        }

    }

}
