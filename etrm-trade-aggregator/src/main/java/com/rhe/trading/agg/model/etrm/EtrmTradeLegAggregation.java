package com.rhe.trading.agg.model.etrm;

import com.rhe.trading.agg.model.canonical.TradeLeg;

import java.util.List;

public record EtrmTradeLegAggregation(List<EtrmTradeLeg> tradeLegs) {

    void add(EtrmTradeLeg tradeLeg) {
        this.tradeLegs.add(tradeLeg);
    }

    void remove(EtrmTradeLeg tradeLeg) {
        this.tradeLegs.remove(tradeLeg);
    }

}
