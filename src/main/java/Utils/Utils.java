package Utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.Currency;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

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

}
