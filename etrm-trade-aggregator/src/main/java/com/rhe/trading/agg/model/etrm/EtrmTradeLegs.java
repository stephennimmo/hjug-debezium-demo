package com.rhe.trading.agg.model.etrm;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class EtrmTradeLegs {

    private List<EtrmTradeLeg> etrmTradeLegs = new CopyOnWriteArrayList<>();

    public EtrmTradeLegs remove(EtrmTradeLeg etrmTradeLeg) {
        Optional<EtrmTradeLeg> optional = getByTradeLegId(etrmTradeLeg.tradeLegId());
        if (optional.isPresent()) {
            etrmTradeLegs.remove(optional.get());
        }
        return this;
    }

    public EtrmTradeLegs update(EtrmTradeLeg etrmTradeLeg) {
        //CAN This be cleaned up?
        switch (etrmTradeLeg.op()) {
            case "c" -> {
                etrmTradeLegs.add(etrmTradeLeg);
            }
            case "u" -> {
                Optional<EtrmTradeLeg> optional = getByTradeLegId(etrmTradeLeg.tradeLegId());
                if (optional.isPresent()) {
                    etrmTradeLegs.remove(optional.get());
                }
                etrmTradeLegs.add(etrmTradeLeg);
            }
            case "d" -> {
                Optional<EtrmTradeLeg> optional = getByTradeLegId(etrmTradeLeg.tradeLegId());
                if (optional.isPresent()) {
                    etrmTradeLegs.remove(optional.get());
                }
            }
        }
        return this;
    }

    private Optional<EtrmTradeLeg> getByTradeLegId(int tradeLegId) {
        return this.etrmTradeLegs.stream().filter(l -> l.tradeLegId() == tradeLegId).findFirst();
    }

    public List<EtrmTradeLeg> getEtrmTradeLegs() {
        return etrmTradeLegs;
    }

    @Override
    public String toString() {
        return "EtrmTradeLegs{" +
                "etrmTradeLegs=" + etrmTradeLegs +
                '}';
    }
}
