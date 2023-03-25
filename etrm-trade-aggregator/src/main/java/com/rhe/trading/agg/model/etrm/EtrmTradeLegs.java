package com.rhe.trading.agg.model.etrm;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EtrmTradeLegs {

    private static final Logger logger = LoggerFactory.getLogger(EtrmTradeLegs.class);

    @JsonProperty
    private Map<Integer, EtrmTradeLeg> etrmTradeLegMap = new LinkedHashMap<>();

    public EtrmTradeLegs update(TradeAndLeg tradeAndLeg) {
        logger.info("Before: {}", etrmTradeLegMap);
        if (tradeAndLeg.getEtrmTradeLeg() != null) {
            logger.info("Adding {} to map with key {}", tradeAndLeg.getEtrmTradeLeg(), tradeAndLeg.getEtrmTradeLeg().tradeLegId());
            etrmTradeLegMap.put(tradeAndLeg.getEtrmTradeLeg().tradeLegId(), tradeAndLeg.getEtrmTradeLeg());
        } else {
            logger.info("Removing key {}", tradeAndLeg.getTradeLegId());
            etrmTradeLegMap.remove(tradeAndLeg.getTradeLegId());
        }
        logger.info("After: {}", etrmTradeLegMap);
        return this;
    }

    public List<EtrmTradeLeg> getEtrmTradeLegs() {
        return new ArrayList<>(this.etrmTradeLegMap.values());
    }

    @Override
    public String toString() {
        return "EtrmTradeLegs{" +
                "etrmTradeLegMap=" + etrmTradeLegMap +
                '}';
    }

}
