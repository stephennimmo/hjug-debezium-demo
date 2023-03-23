package com.rhe.trading.agg.model.etrm;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EtrmTradeHeader(
        @JsonProperty("trade_id") int tradeId, @JsonProperty("start_date") int startDate, @JsonProperty("end_date") int endDate,
        @JsonProperty("execution_timestamp") long executionTimestamp, @JsonProperty("trade_type_id") int tradeTypeId,
        @JsonProperty("__op") String op, @JsonProperty("__source_txId") int transactionId, @JsonProperty("__source_table") String sourceTable,
        @JsonProperty("__source_ts_ms") long timestamp, @JsonProperty("__deleted") String deleted
) {
}
