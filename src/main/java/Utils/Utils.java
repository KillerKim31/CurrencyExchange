package Utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.Currency;
import repositories.CurrencyCrud;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Utils {

    public static String getRequestBodyString(HttpServletRequest req){
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString().trim();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static HashMap<String, Object> getRequestBodyJSON(HttpServletRequest req) {
        try {
            String requestBody = getRequestBodyString(req);
            if (requestBody.isEmpty()) return null;
            // Десериализация JSON в HashMap
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(requestBody, HashMap.class);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения JSON из запроса", e);
        }
    }

    public static void generateError(HttpServletResponse resp, int ErrorCode, ObjectMapper mapper, String errorText) throws IOException {
        resp.setStatus(ErrorCode);
        Map<String, Object> errorObject = new HashMap<>();
        errorObject.put("error", errorText);
        mapper.writeValue(resp.getWriter(), errorObject);
    }

    public static boolean isValidCurrencyEntry(Currency entry) {
        if (entry.getCode() == null || entry.getCode().isBlank()) {
            return false;
        }
        if (entry.getFullName() == null || entry.getFullName().isBlank()) {
            return false;
        }
        if (entry.getSign() == null || entry.getSign().toString().isBlank()) {
            return false;
        }
        return true;
    }

    public static boolean isValidExchangeJSON(Map<String, Object> jsonObject) {

        String baseCurrencyCode   = jsonObject.get("baseCurrencyCode").toString();
        String targetCurrencyCode = jsonObject.get("targetCurrencyCode").toString();
        CurrencyCrud currencyCrud = new CurrencyCrud();

        if (baseCurrencyCode == null || baseCurrencyCode.isEmpty()) {
            if (currencyCrud.findByCode(baseCurrencyCode) == null)
                return false;
        }
        if (targetCurrencyCode == null || targetCurrencyCode.isEmpty()) {
            if (currencyCrud.findByCode(targetCurrencyCode) == null)
                return false;
        }
        if (jsonObject.get("rate") != null) {
            try {
                Double.parseDouble(jsonObject.get("rate").toString());
            }
            catch (Exception e) {
                return false;
            }
        }
        return true;

    }

    public static boolean isValidExchangeArgs(Map<String, String[]> args) {

        String from   = (args.get("from") != null && args.get("from").length > 0) ? args.get("from")[0] : null;
        String to     = (args.get("to") != null && args.get("to").length > 0) ? args.get("to")[0] : null;
        String amount = (args.get("amount") != null && args.get("amount").length > 0) ? args.get("amount")[0] : null;

        if (from == null || from.isEmpty()) {
            return false;
        }
        if (from == null || to.isEmpty()) {
            return false;
        }
        if (from == null || amount.isEmpty()) {
            try {
                Double.parseDouble(amount);
            }
            catch (Exception e) {
                return false;
            }
        }

        return true;

    }

}
