package servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.Currency;
import repositories.CurrencyCrud;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TestServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        super.init();
        getServletContext().log("TestServlet initialized on application startup");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain;charset=UTF-8");
        resp.getWriter().println("TestServlet is working. Application started successfully.");
        testCrud(resp);
    }

    private void testCrud(HttpServletResponse resp) throws IOException {

        var currencyCrud = new CurrencyCrud();

        resp.getWriter().println("Test findAll");
        var listCurrency = currencyCrud.findAll();
        for (int i = 0; i < listCurrency.size(); i++) {
            var entry = listCurrency.get(i);
            String result = new ObjectMapper().writeValueAsString(entry);
            resp.getWriter().println("i = " + i + " " + result);
        }

        resp.getWriter().println("");
        resp.getWriter().println("Test save");
        var newEntry = new Currency("TEST", "TEST_NAME", 'Q');
        Long idEntry = currencyCrud.save(newEntry);

        resp.getWriter().println("");
        resp.getWriter().println("Test findById");
        var entry = currencyCrud.findById(idEntry);
        if (entry != null) {
            String result = new ObjectMapper().writeValueAsString(entry);
            resp.getWriter().println("entry = " + result);
        }

        resp.getWriter().println("");
        resp.getWriter().println("Test update");
        entry.setCode("OTHER_CODE");
        entry.setFullName("TEST_OTHER_NAME");
        currencyCrud.update(entry);
        entry = currencyCrud.findById(idEntry);
        if (entry != null) {
            String result = new ObjectMapper().writeValueAsString(entry);
            resp.getWriter().println("entry = " + result);
        }

        resp.getWriter().println("");
        resp.getWriter().println("Test delete");
        //currencyCrud.delete(idEntry);
    }
}

