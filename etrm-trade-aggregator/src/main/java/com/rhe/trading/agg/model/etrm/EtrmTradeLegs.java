package com.rhe.trading.agg.model.etrm;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.debezium.data.Envelope;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EtrmTradeLegs {

    @JsonProperty
    private Map<Integer, EtrmTradeLeg> etrmTradeLegMap = new LinkedHashMap<>();

    public EtrmTradeLegs update(EtrmTradeLeg etrmTradeLeg) {
        if (etrmTradeLeg.op().equals(Envelope.Operation.DELETE.code())) {
            etrmTradeLegMap.remove(etrmTradeLeg.tradeLegId());
        } else {
            etrmTradeLegMap.put(etrmTradeLeg.tradeLegId(), etrmTradeLeg);
        }
        return this;
    }

    public List<EtrmTradeLeg> getEtrmTradeLegs() {
        return new ArrayList<>(this.etrmTradeLegMap.values());
    }

    @Override
    public String toString() {
        return "EtrmTradeLegs[" +
                "etrmTradeLegMap=" + etrmTradeLegMap +
                ']';
    }

}
