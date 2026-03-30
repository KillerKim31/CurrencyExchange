package DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import models.Currency;
import models.Exchange;

@JsonPropertyOrder({"baseCurrency", "targetCurrency", "rate", "amount", "convertedAmount"})
@JsonIgnoreProperties({"id"})  // Игнорировать поле id
public class ExchangeDTO extends Exchange {

    // Дополнительные поля
    private Double      amount;           // Исходное значение валюты
    private Double      convertedAmount;  // Преобразованное значение валюты


    public ExchangeDTO(Exchange exchange, Double amount, Double convertedAmount) {
        baseCurrency         = exchange.getBaseCurrency();
        targetCurrency       = exchange.getTargetCurrency();
        rate                 = exchange.getRate();
        this.amount          = amount;
        this.convertedAmount = convertedAmount;
    }

    public ExchangeDTO(Currency currency_A, Currency currency_B, Double rate, Double amount, Double convertedAmount) {
        baseCurrency         = currency_A;
        targetCurrency       = currency_B;
        this.rate            = rate;
        this.amount          = amount;
        this.convertedAmount = convertedAmount;
    }

    public Double getConvertedAmount() {
        return convertedAmount;
    }

    public void setConvertedAmount(Double convertedAmount) {
        this.convertedAmount = convertedAmount;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

}