package com.rhe.trading.agg.model.canonical;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Trade {

    private int tradeId;

    private List<TradeLeg> tradeLegs = new ArrayList<>();

    public void add(TradeLeg tradeLeg) {
        this.tradeLegs.add(tradeLeg);
    }

    public void remove(TradeLeg tradeLeg) {
        this.tradeLegs.remove(tradeLeg);
    }

    public int getTradeId() {
        return tradeId;
    }

    public void setTradeId(int tradeId) {
        this.tradeId = tradeId;
    }

    public List<TradeLeg> getTradeLegs() {
        return tradeLegs;
    }

    public void setTradeLegs(List<TradeLeg> tradeLegs) {
        this.tradeLegs = tradeLegs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trade trade = (Trade) o;
        return tradeId == trade.tradeId && Objects.equals(tradeLegs, trade.tradeLegs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tradeId, tradeLegs);
    }

    @Override
    public String toString() {
        return "Trade{" +
                "tradeId=" + tradeId +
                ", tradeLegs=" + tradeLegs +
                '}';
    }

}
