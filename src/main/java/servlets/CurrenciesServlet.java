package servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.Currency;
import repositories.CurrencyCrud;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;


@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        new ObjectMapper().writeValue(resp.getWriter(), new CurrencyCrud().findAll());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        ObjectMapper mapper = new ObjectMapper();
        Currency newEntry;

        // Чтение и десериализация JSON
        try {
            String requestBody = Utils.Utils.getRequestBodyString(req);
            newEntry = mapper.readValue(requestBody, Currency.class);
        } catch (IOException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(resp.getWriter(), Map.of(
                    "error", "Неверно введены данные. Пример: code = \"USD\", fullName = \"US Dollar\", sign = \"$\""
            ));
            return;
        }

        // Проверка на дубликат
        var entryCrud = new CurrencyCrud();
        if (entryCrud.findByCode(newEntry.getCode()) != null) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            mapper.writeValue(resp.getWriter(), Map.of(
                    "error", "Запись с таким кодом уже существует."
            ));
            return;
        }

        // Проверка валидности полей
        if (!Utils.Utils.isValidCurrencyEntry(newEntry)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(resp.getWriter(), Map.of(
                    "error", "Неверно введены данные. Пример: code = \"USD\", fullName = \"US Dollar\", sign = \"$\""
            ));
            return;
        }

        // Сохранение в БД
        Long id = entryCrud.save(newEntry);
        if (id == null || id <= 0) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(resp.getWriter(), Map.of(
                    "error", "Ошибка при добавлении записи в БД."
            ));
            return;
        }

        // Успешное создание записи
        resp.setStatus(HttpServletResponse.SC_CREATED);
        mapper.writeValue(resp.getWriter(), Map.of("id", id));

    }
}
