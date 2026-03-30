package servlets;

import DTO.ExchangeDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Currency;
import models.Exchange;
import repositories.CurrencyCrud;
import repositories.ExchangeCrud;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DecimalFormat;

@WebServlet("/exchange")
public class EchangeServlet extends HttpServlet {

    private final String ERR_MSG_INCORRECT_DATA     = """
                                                         Неверно введены данные, необходимо в URL параметрах передать 3 обязательных параметра.
                                                         Пример: from=USD&to=AUD&amount=10
                                                      """;
    private final String ERR_MSG_INNER              = "Внутренняя ошибка сервера.";
    private final String ERR_MSG_DATABASE           = "Ошибка при взаимодействии с БД.";
    private final String ERR_MSG_CURRENCY_NOT_FOUND = "Указана несуществующая валюта.";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        ObjectMapper mapper = new ObjectMapper();
        CurrencyCrud currencyCrud = new CurrencyCrud();

        try {

            // Проверка параметров в URL
            if (!Utils.Utils.isValidExchangeArgs(req.getParameterMap())) {
                Utils.Utils.generateError(resp, HttpServletResponse.SC_BAD_REQUEST, mapper, ERR_MSG_INCORRECT_DATA);
                return;
            }
            String from   = req.getParameter("from");
            String to     = req.getParameter("to");
            Double amount = Double.parseDouble(req.getParameter("amount"));

            // Получение валют из БД
            Currency fromCurrency = null;
            Currency toCurrency   = null;
            try {
                fromCurrency = currencyCrud.findByCode(from);
                toCurrency = currencyCrud.findByCode(to);
            }
            catch (Exception e) {
                Utils.Utils.generateError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, mapper, ERR_MSG_DATABASE);
            }

            if (fromCurrency == null || toCurrency == null) {
                Utils.Utils.generateError(resp, HttpServletResponse.SC_NOT_FOUND, mapper, ERR_MSG_CURRENCY_NOT_FOUND);
                return;
            }

            // Вычисление курса обмена валюты
            try {
                Double rate = 0.0;
                try {
                    rate = Math.round(getActualExchangeRate(from, to) * 100.0) / 100.0;
                }
                catch (Exception e) {
                    Utils.Utils.generateError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, mapper, ERR_MSG_DATABASE);
                }
                Double convertedAmount = Math.round(amount * rate * 100.0) / 100.0;
                ExchangeDTO exchangeDTO = new ExchangeDTO(fromCurrency, toCurrency, rate, amount, convertedAmount);
                mapper.writeValue(resp.getWriter(), exchangeDTO);
            }
            catch (Exception e){
                Utils.Utils.generateError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, mapper, ERR_MSG_INNER);
                return;
            }

        }
        catch (Exception e) {
            Utils.Utils.generateError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, mapper, ERR_MSG_INNER);
        }

    }

    private Double getActualExchangeRate(String from, String to) {

        Exchange resultExchange;
        ExchangeCrud exchangeCrud = new ExchangeCrud();

        // Получение прямого курса
        if ((resultExchange = exchangeCrud.findByCodes(from, to)) != null) {
            return resultExchange.getRate();
        }
        // Получение обратного курса
        else if ((resultExchange = exchangeCrud.findByCodes(to, from)) != null) {
            return 1 / resultExchange.getRate();
        }
        // Вычисление курса через доллар
        else {
            Exchange exchangeUSD_A = exchangeCrud.findByCodes("USD", from);
            Exchange exchangeUSD_B = exchangeCrud.findByCodes("USD", to);
            if (exchangeUSD_A != null || exchangeUSD_B != null ) {
                return exchangeUSD_B.getRate() / exchangeUSD_A.getRate();
            }
        }

        return 0.0;
    }

}
