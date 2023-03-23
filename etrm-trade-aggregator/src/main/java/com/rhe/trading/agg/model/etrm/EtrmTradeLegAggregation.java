package com.rhe.trading.agg.model.etrm;

import java.util.*;

public class EtrmTradeLegAggregation {

    private final List<EtrmTradeLeg> etrmTradeLegs = new ArrayList<>();

    public EtrmTradeLegAggregation update(EtrmTradeLegEnvelope etrmTradeLegEnvelope) {
        switch (etrmTradeLegEnvelope.getOp()) {
            case "c" -> {
                etrmTradeLegs.add(etrmTradeLegEnvelope.getAfter());
            }
            case "u" -> {
                Optional<EtrmTradeLeg> optional = etrmTradeLegs.stream().filter(leg -> leg.tradeLegId() == etrmTradeLegEnvelope.getAfter().tradeLegId()).findFirst();
                if (optional.isPresent()) {
                    etrmTradeLegs.remove(optional.get());
                }
                etrmTradeLegs.add(etrmTradeLegEnvelope.getAfter());
            }
            case "d" -> {
                Optional<EtrmTradeLeg> optional = etrmTradeLegs.stream().filter(leg -> leg.tradeLegId() == etrmTradeLegEnvelope.getAfter().tradeLegId()).findFirst();
                if (optional.isPresent()) {
                    etrmTradeLegs.remove(optional.get());
                }
            }
        }
        return this;
    }

    public List<EtrmTradeLeg> getEtrmTradeLegMap() {
        return etrmTradeLegs;
    }

    @Override
    public String toString() {
        return "EtrmTradeLegAggregation{" +
                "etrmTradeLegs=" + etrmTradeLegs +
                '}';
    }
}
