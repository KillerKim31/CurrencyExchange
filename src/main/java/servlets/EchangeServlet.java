package servlets;

import DTO.ExchangeDTO;
import Exceptions.DatabaseException;
import Exceptions.InvalidEntryException;
import Exceptions.NotFoundEntryException;
import Service.ExchangeService;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet("/exchange")
public class EchangeServlet extends BaseServlet {

    private ExchangeService entryService;

    private final String ERR_MSG_INCORRECT_DATA     = """
                                                         Неверно введены данные, необходимо в URL параметрах передать 3 обязательных параметра.
                                                         Пример: from=USD&to=AUD&amount=10
                                                      """;
    protected final String ERR_MSG_CURRENCY_NOT_FOUND = "Указанная валюта не найдена.";
    protected final String ERR_MSG_DATABASE           = "Ошибка при взаимодействии с БД.";
    protected final String ERR_MSG_INNER              = "Внутренняя ошибка сервера.";

    @Override
    public void init() throws ServletException {
        super.init();
        entryService = new ExchangeService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        try {
            ExchangeDTO exchangeDTOEntry = entryService.getExchangeDTO(req.getParameterMap());
            resp.setStatus(HttpServletResponse.SC_OK);
            mapper.writeValue(resp.getWriter(), exchangeDTOEntry);
        }
        catch (InvalidEntryException | IOException e) {
            generateError(resp, HttpServletResponse.SC_BAD_REQUEST, mapper, ERR_MSG_INCORRECT_DATA);
        } catch (NotFoundEntryException e) {
            generateError(resp, HttpServletResponse.SC_CONFLICT, mapper, ERR_MSG_CURRENCY_NOT_FOUND);
        } catch (DatabaseException e) {
            generateError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, mapper, ERR_MSG_DATABASE);
        } catch (Exception e) {
            generateError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, mapper, ERR_MSG_INNER);
        }

    }

}
