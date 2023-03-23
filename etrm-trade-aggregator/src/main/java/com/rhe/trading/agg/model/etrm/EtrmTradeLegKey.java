package com.rhe.trading.agg.model.etrm;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EtrmTradeLegKey(@JsonProperty("trade_leg_id") int tradeLegId) {
}
