package com.rhe.trading.agg.model.etrm;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class EtrmTradeLegs {

    private static final Logger logger = LoggerFactory.getLogger(EtrmTradeLegs.class);

    @JsonProperty
    private Map<Integer, EtrmTradeLeg> etrmTradeLegMap = new LinkedHashMap<>();

    public EtrmTradeLegs update(EtrmTradeIdToTradeLegMapping mapping) {
        logger.info("Before Keys: {}", getKeys());
        if (mapping.getEtrmTradeLeg() != null) {
            logger.info("Adding {} to map with key {}", mapping.getEtrmTradeLeg(), mapping.getEtrmTradeLeg().tradeLegId());
            etrmTradeLegMap.put(mapping.getEtrmTradeLeg().tradeLegId(), mapping.getEtrmTradeLeg());
        } else {
            logger.info("Removing key {}", mapping.getTradeLegId());
            etrmTradeLegMap.remove(mapping.getTradeLegId());
        }
        logger.info("After Keys: {}", getKeys());
        return this;
    }

    public List<EtrmTradeLeg> getEtrmTradeLegs() {
        return new ArrayList<>(this.etrmTradeLegMap.values());
    }

    @JsonIgnore
    public Set<Integer> getKeys() {
        return this.etrmTradeLegMap.keySet();
    }

    @Override
    public String toString() {
        return "EtrmTradeLegs{" +
                "etrmTradeLegMap=" + etrmTradeLegMap +
                '}';
    }

}
