package com.rhe.trading.agg.model.canonical;

import java.util.Objects;

public class TradeLeg {

    private int tradeLegId;

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
        TradeLeg tradeLeg = (TradeLeg) o;
        return tradeLegId == tradeLeg.tradeLegId && tradeId == tradeLeg.tradeId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tradeLegId, tradeId);
    }

    @Override
    public String toString() {
        return "TradeLeg{" +
                "tradeLegId=" + tradeLegId +
                ", tradeId=" + tradeId +
                '}';
    }

}
