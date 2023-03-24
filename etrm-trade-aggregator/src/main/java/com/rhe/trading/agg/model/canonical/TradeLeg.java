package com.rhe.trading.agg.model.canonical;

import java.math.BigDecimal;

public record TradeLeg (int tradeLegId, int tradeId, int payerId, int receiverId, int commodityId, int locationId,
                        BigDecimal price, int priceCurrencyId, BigDecimal quantity, int quantityUomId
) {

}
