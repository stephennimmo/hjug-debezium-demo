package com.rhe.trading.agg.model.etrm;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record EtrmTransaction (
        @JsonProperty("status") String status, @JsonProperty("id") String id, @JsonProperty("event_count") Integer eventCount,
        @JsonProperty("data_collections") List<DataCollection> dataCollections, @JsonProperty("ts_ms") long timestamp
){
}
