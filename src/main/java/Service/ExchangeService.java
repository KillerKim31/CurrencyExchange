package Service;

import DTO.ExchangeDTO;
import Exceptions.DatabaseException;
import Exceptions.DuplicateEntryException;
import Exceptions.InvalidEntryException;
import Exceptions.NotFoundEntryException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Currency;
import models.Exchange;
import repositories.CurrencyCrud;
import repositories.ExchangeCrud;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ExchangeService {

    private ExchangeCrud exchangeCrud;
    private CurrencyCrud currencyCrud;

    public ExchangeService() {
        exchangeCrud = new ExchangeCrud();
        currencyCrud = new CurrencyCrud();
    }

    public ExchangeDTO getExchangeDTO(Map<String, String[]> parameterMap) throws InvalidEntryException, NotFoundEntryException, DatabaseException {

        // Валидация параметров в URL
        String from      = parameterMap.getOrDefault("from"  , new String[]{""})[0];
        String to        = parameterMap.getOrDefault("to"    , new String[]{""})[0];
        String amountStr = parameterMap.getOrDefault("amount", new String[]{""})[0];
        if (!isValidExchangeArgs(from, to, amountStr)) {
            throw new InvalidEntryException();
        }
        Double amount = Double.parseDouble(amountStr);

        Currency fromCurrency = fromCurrency = currencyCrud.findByCode(from);
        Currency toCurrency   = toCurrency   = currencyCrud.findByCode(to);
        if (fromCurrency == null || toCurrency == null) {
            throw new NotFoundEntryException();
        }

        double rate = Math.round(getActualExchangeRate(from, to) * 100.0) / 100.0;
        double convertedAmount = Math.round(amount * rate * 100.0) / 100.0;
        return new ExchangeDTO(fromCurrency, toCurrency, rate, amount, convertedAmount);

    }

    public List<Exchange> getExchangeList() {
        return exchangeCrud.findAll();
    }

    public Exchange getExchange(String pathInfo) throws InvalidEntryException, NotFoundEntryException {

        if (!isValidUrlCurrencies(pathInfo)) throw new InvalidEntryException();
        var baseCurrencyCode   = pathInfo.substring(1, 4).toUpperCase();
        var targetCurrencyCode = pathInfo.substring(4).toUpperCase();

        Exchange exchangeEntry = exchangeCrud.findByCodes(baseCurrencyCode, targetCurrencyCode);
        // Проверяем нашлись ли записи по обменному курсу
        if (exchangeEntry == null) {
            throw new NotFoundEntryException("Запись по обменному курсу не найдена в БД");
        }

        return exchangeEntry;

    }

    public Exchange updateExchangeRate(String pathInfo, String requestBody)
            throws InvalidEntryException, NotFoundEntryException, JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        HashMap jsonObject = mapper.readValue(requestBody, HashMap.class);

        // Валидация и парсинг значений из URL и тела запроса
        if (!isValidUrlCurrencies(pathInfo) || !isValidExchangeRate(jsonObject)) throw new InvalidEntryException();
        String baseCurrencyCode = pathInfo.substring(1, 4).toUpperCase();
        String targetCurrencyCode = pathInfo.substring(4).toUpperCase();
        Double rate = Double.parseDouble(jsonObject.get("rate").toString());

        Exchange exchangeEntry = exchangeCrud.findByCodes(baseCurrencyCode, targetCurrencyCode);
        if (exchangeEntry == null) {
            throw new NotFoundEntryException();
        }
        exchangeEntry.setRate(rate);
        exchangeCrud.update(exchangeEntry);
        return exchangeEntry;

    }

    public Long createExchange(String pathInfo, String requestBody)
            throws InvalidEntryException, DuplicateEntryException, NotFoundEntryException, DatabaseException, JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        HashMap jsonObject = mapper.readValue(requestBody, HashMap.class);

        // Валидация и парсинг значений из URL и тела запроса
        if (!isValidExchangeJSON(jsonObject)) throw new InvalidEntryException();
        String baseCurrencyCode   = jsonObject.get("baseCurrencyCode").toString();
        String targetCurrencyCode = jsonObject.get("targetCurrencyCode").toString();
        Double rate               = Double.parseDouble(jsonObject.get("rate").toString());

        // Проверка на дубликат
        if (exchangeCrud.findByCodes(baseCurrencyCode, targetCurrencyCode) != null) {
            throw new DuplicateEntryException("Обменный курс с кодами " + baseCurrencyCode + targetCurrencyCode + " уже существует");
        }

        var currencyCrud   = new CurrencyCrud();
        var baseCurrency   = currencyCrud.findByCode(baseCurrencyCode);
        var targetCurrency = currencyCrud.findByCode(targetCurrencyCode);

        if (baseCurrency == null || targetCurrency == null) {
            throw new NotFoundEntryException("одна либо более валют с кодами " + baseCurrencyCode + ", " + targetCurrencyCode + " не найдены.");
        }

        Exchange newEntry = new Exchange(baseCurrency,
                targetCurrency,
                rate);

        // Сохранение в БД
        Long id = exchangeCrud.save(newEntry);
        if (id == null || id <= 0) {
            throw new DatabaseException();
        }
        return id;

    }


    private boolean isValidExchangeJSON(Map<String, Object> jsonObject) {

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

    private boolean isValidExchangeArgs(String from, String to, String amount) {
        if (from == null || from.isBlank() || to == null || to.isBlank() || amount == null || amount.isBlank()) {
            return false;
        }
        try {
            Double.parseDouble(amount);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    private boolean isValidUrlCurrencies(String pathInfo) throws InvalidEntryException {
        if (pathInfo == null || pathInfo.length() != 7) {
            return false;
        }
        return true;
    }

    private boolean isValidExchangeRate(HashMap jsonObject) throws InvalidEntryException {
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

    private Double getActualExchangeRate(String from, String to) throws DatabaseException {

        try {
            Exchange direct = exchangeCrud.findByCodes(from, to);
            if (direct != null) return direct.getRate();

            Exchange reverse = exchangeCrud.findByCodes(to, from);
            if (reverse != null) return 1 / reverse.getRate();

            Exchange usdFrom = exchangeCrud.findByCodes("USD", from);
            Exchange usdTo   = exchangeCrud.findByCodes("USD", to);
            if (usdFrom != null && usdTo != null) {
                return usdTo.getRate() / usdFrom.getRate();
            }

            return 0.0;
        } catch (Exception e) {
            throw new DatabaseException("Ошибка при получении курса обмена", e);
        }

    }

}
