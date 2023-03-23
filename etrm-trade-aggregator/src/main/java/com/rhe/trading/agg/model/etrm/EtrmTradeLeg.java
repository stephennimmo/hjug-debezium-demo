package com.rhe.trading.agg.model.etrm;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record EtrmTradeLeg(
        @JsonProperty("trade_leg_id") int tradeLegId, @JsonProperty("trade_id") int tradeId,
        @JsonProperty("payer_id") int payerId, @JsonProperty("receiver_id") int receiverId,
        @JsonProperty("commodity_id") int commodity_id, @JsonProperty("location_id") int locationId,
        @JsonProperty("price") BigDecimal price, @JsonProperty("price_currency_id") int priceCurrencyId,
        @JsonProperty("quantity") BigDecimal quantity, @JsonProperty("quantity_uom_id") int quantityUomId
) {

}
