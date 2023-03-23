package com.rhe.trading.agg.model.etrm;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DataCollection(@JsonProperty("data_collection") String dataCollection, @JsonProperty("event_count") int eventCount) {
}
