package servlets;

import Exceptions.DatabaseException;
import Exceptions.DuplicateEntryException;
import Exceptions.InvalidEntryException;
import Service.CurrencyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Currency;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;


@WebServlet("/currencies")
public class CurrenciesServlet extends BaseServlet {

    private CurrencyService currencyService;

    protected final String ERR_MSG_INCORRECT_ENDPOINT = "Неверно введены данные. Пример: code = \"USD\", fullName = \"US Dollar\", sign = \"$\"";
    protected final String ERR_DUPLICATE_ENTRY        = "Запись с таким кодом уже существует.";
    protected final String ERR_MSG_DATABASE           = "Ошибка при взаимодействии с БД.";
    protected final String ERR_MSG_INNER              = "Внутренняя ошибка сервера.";

    @Override
    public void init() throws ServletException {
        super.init();
        currencyService = new CurrencyService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            new ObjectMapper().writeValue(resp.getWriter(), currencyService.getCurrencyList());
        } catch (DatabaseException e) {
            generateError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, mapper, ERR_MSG_DATABASE);
        } catch (Exception e) {
            generateError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, mapper, ERR_MSG_INNER);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ObjectMapper mapper = new ObjectMapper();

        try {
            CurrencyService currencyService = new CurrencyService();
            Currency newEntry = mapper.readValue(Utils.Utils.getRequestBodyString(req), Currency.class);
            Long id = currencyService.createCurrency(newEntry);

            resp.setStatus(HttpServletResponse.SC_CREATED);
            mapper.writeValue(resp.getWriter(), Map.of("id", id));
        } catch (InvalidEntryException | IOException e) {
            generateError(resp, HttpServletResponse.SC_BAD_REQUEST, mapper, ERR_MSG_INCORRECT_ENDPOINT);
        } catch (DuplicateEntryException e) {
            generateError(resp, HttpServletResponse.SC_CONFLICT, mapper, ERR_DUPLICATE_ENTRY);
        } catch (DatabaseException e) {
            generateError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, mapper, ERR_MSG_DATABASE);
        } catch (Exception e) {
            generateError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, mapper, ERR_MSG_INNER);
        }

    }
}
