package Service;

import Exceptions.DatabaseException;
import Exceptions.DuplicateEntryException;
import Exceptions.InvalidEntryException;
import models.Currency;
import repositories.CurrencyCrud;

import java.util.List;


public class CurrencyService {

    private CurrencyCrud currencyCrud;

    public CurrencyService() {
        currencyCrud = new CurrencyCrud();

    }

    public List<Currency> getCurrencyList() throws DatabaseException {
        return currencyCrud.findAll();
    }

    public Currency getCurrencyEntry(String currencyCode) throws DatabaseException {
        return currencyCrud.findByCode(currencyCode);
    }

    public Long createCurrency(Currency newEntry) throws DuplicateEntryException, InvalidEntryException, DatabaseException {

        // Проверка, что объект не null
        if (newEntry == null) {
            throw new InvalidEntryException("Объект Currency не может быть null");
        }
        // Проверка валидности полей
        if (!isValidCurrencyEntry(newEntry)) {
            throw new InvalidEntryException();
        }

        // Проверка на дубликат
        if (currencyCrud.findByCode(newEntry.getCode()) != null) {
            throw new DuplicateEntryException("Валюта с кодом " + newEntry.getCode() + " уже существует");
        }
        // Сохранение в базе
        Long id = currencyCrud.save(newEntry);
        if (id == null || id <= 0) {
            throw new DatabaseException("Ошибка при добавлении записи в базу данных");
        }
        return id;

    }

    private boolean isValidCurrencyEntry(Currency entry) {
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
