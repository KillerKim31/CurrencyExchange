package models;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "id", "baseCurrencyId", "targetCurrencyId", "rate" })
public class Exchange {

    private Long   id;
    private Long   baseCurrencyId;
    private Long   targetCurrencyId;
    private Double rate;

    public Exchange() {
    }

    public Exchange(Long baseCurrencyId, Long targetCurrencyId, Double rate) {
        this.baseCurrencyId   = baseCurrencyId;
        this.targetCurrencyId = targetCurrencyId;
        this.rate             = rate;
    }

    public Exchange(Long id, Long baseCurrencyId, Long targetCurrencyId, Double rate) {
        this.id               = id;
        this.baseCurrencyId   = baseCurrencyId;
        this.targetCurrencyId = targetCurrencyId;
        this.rate             = rate;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBaseCurrencyId() {
        return baseCurrencyId;
    }

    public void setBaseCurrencyId(Long baseCurrencyId) {
        this.baseCurrencyId = baseCurrencyId;
    }

    public Long getTargetCurrencyId() {
        return targetCurrencyId;
    }

    public void setTargetCurrencyId(Long targetCurrencyId) {
        this.targetCurrencyId = targetCurrencyId;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

}
