package com.rhe.trading.agg.model.etrm;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Transaction(
        @JsonProperty("id") String id, @JsonProperty("total_order") int totalOrder,
        @JsonProperty("data_collection_order") int dataCollectionOrder
) {
}
