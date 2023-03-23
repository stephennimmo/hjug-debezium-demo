package com.rhe.trading.agg.model.etrm;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EtrmTradeHeaderKey(@JsonProperty("trade_id") int tradeId) {
}
