package Utils;

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

}
