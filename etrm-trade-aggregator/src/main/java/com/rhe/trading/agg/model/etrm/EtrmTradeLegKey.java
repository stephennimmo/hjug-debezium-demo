package com.rhe.trading.agg.model.etrm;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Objects;

public class EtrmTradeLegKey {

    @JsonProperty("trade_leg_id")
    private int tradeLegId;

    @JsonProperty("trade_id")
    private int tradeId;

    public int getTradeLegId() {
        return tradeLegId;
    }

    public void setTradeLegId(int tradeLegId) {
        this.tradeLegId = tradeLegId;
    }

    public int getTradeId() {
        return tradeId;
    }

    public void setTradeId(int tradeId) {
        this.tradeId = tradeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EtrmTradeLegKey that = (EtrmTradeLegKey) o;
        return tradeLegId == that.tradeLegId && tradeId == that.tradeId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tradeLegId, tradeId);
    }

    @Override
    public String toString() {
        return "EtrmTradeLegKey{" +
                "tradeLegId=" + tradeLegId +
                ", tradeId=" + tradeId +
                '}';
    }

}
