package com.rhe.trading.agg.model.etrm;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TradeIdAndTransactionId(@JsonProperty int tradeId, @JsonProperty int transactionId) {
}
