package models;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "id", "baseCurrencyId", "targetCurrencyId", "rate" })
public class Exchange {

    private Long   id;
    private Currency   baseCurrency;
    private Currency   targetCurrency;
    private Double rate;

    public Exchange() {
    }

    public Exchange(Currency baseCurrency, Currency targetCurrency, Double rate) {
        this.baseCurrency   = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.rate           = rate;
    }

    public Exchange(Long id, Currency baseCurrency, Currency targetCurrency, Double rate) {
        this.id               = id;
        this.baseCurrency   = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.rate             = rate;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Currency getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrencyId(Currency baseCurrencyId) {
        this.baseCurrency = baseCurrency;
    }

    public Currency getTargetCurrency() {
        return targetCurrency;
    }

    public void setTargetCurrencyId(Currency targetCurrencyId) {
        this.targetCurrency = targetCurrency;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

}
