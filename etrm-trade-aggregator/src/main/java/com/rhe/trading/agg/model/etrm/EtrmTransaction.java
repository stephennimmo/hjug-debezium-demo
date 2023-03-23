package com.rhe.trading.agg.model.etrm;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record EtrmTransaction (
        @JsonProperty("status") String status, @JsonProperty("id") String id, @JsonProperty("event_count") int eventCount,
        @JsonProperty("data_collections") List<DataCollection> dataCollections, @JsonProperty("ts_ms") long timestamp
){
}
