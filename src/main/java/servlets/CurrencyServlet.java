package servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.Currency;
import repositories.CurrencyCrud;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;


@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        // получаем код валюты
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Не указан код валюты\"}");
            return;
        }

        // удаляем слеш в начале
        String currencyCode = pathInfo.substring(1).toUpperCase();

        // ищем валюту в базе
        CurrencyCrud currencyCrud = new CurrencyCrud();
        Currency currency = currencyCrud.findByCode(currencyCode);

        ObjectMapper mapper = new ObjectMapper();
        if (currency == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            mapper.writeValue(resp.getWriter(), Map.of(
                    "error", "Валюта с кодом " + currencyCode + " не найдена"
            ));
            return;
        }

        // возвращаем объект валюты в JSON
        resp.setStatus(HttpServletResponse.SC_OK);
        mapper.writeValue(resp.getWriter(), currency);

    }

}
