package com.rhe.trading.agg.model.etrm;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public record EtrmTradeHeader(
        @JsonProperty("trade_id") int tradeId, @JsonProperty("start_date") int startDate, @JsonProperty("end_date") int endDate,
        @JsonProperty("execution_timestamp") long executionTimestamp, @JsonProperty("trade_type_id") int tradeTypeId
) {
}
