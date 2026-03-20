package Utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.Currency;
import models.Exchange;
import repositories.CurrencyCrud;

import javax.servlet.http.HttpServletRequest;
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

    public static boolean isNotValidCurrencyEntry(Currency entry) {
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

    public static boolean isNotValidExchangeJSON(Map<String, Object> jsonObject) {

        String baseCurrencyCode   = jsonObject.get("baseCurrencyCode").toString();
        String targetCurrencyCode = jsonObject.get("targetCurrencyCode").toString();
        CurrencyCrud currencyCrud = new CurrencyCrud();

        if (baseCurrencyCode.isEmpty()) {
            if (currencyCrud.findByCode(baseCurrencyCode) == null)
                return false;
        }
        if (targetCurrencyCode.isEmpty()) {
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

}
