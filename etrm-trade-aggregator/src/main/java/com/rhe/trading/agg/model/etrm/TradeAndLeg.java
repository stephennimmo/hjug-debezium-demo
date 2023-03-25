package com.rhe.trading.agg.model.etrm;

import io.debezium.data.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TradeAndLeg {

    private static final Logger logger = LoggerFactory.getLogger(TradeAndLeg.class);

    private int tradeLegId;
    private int tradeId;
    private EtrmTradeLeg etrmTradeLeg;

    public TradeAndLeg update(int tradeLegId, int tradeId, EtrmTradeLeg etrmTradeLeg) {
        logger.info("Before: {}, {}, {}", this.tradeLegId, this.tradeId, this.etrmTradeLeg);
        this.tradeLegId = tradeLegId;
        if (!etrmTradeLeg.op().equals(Envelope.Operation.DELETE.code())) {
            this.tradeId = tradeId;
        }
        this.etrmTradeLeg = etrmTradeLeg.op().equals(Envelope.Operation.DELETE.code()) ? null : etrmTradeLeg;
        logger.info("After: {}, {}, {}", this.tradeLegId, this.tradeId, this.etrmTradeLeg);
        return this;
    }

    public int getTradeLegId() {
        return tradeLegId;
    }

    public int getTradeId() {
        return tradeId;
    }

    public EtrmTradeLeg getEtrmTradeLeg() {
        return etrmTradeLeg;
    }

    @Override
    public String toString() {
        return "TradeAndLeg{" +
                "tradeLegId=" + tradeLegId +
                ", tradeId=" + tradeId +
                ", etrmTradeLeg=" + etrmTradeLeg +
                '}';
    }

}
