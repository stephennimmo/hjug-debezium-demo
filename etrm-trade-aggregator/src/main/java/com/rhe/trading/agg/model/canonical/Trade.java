package com.rhe.trading.agg.model.canonical;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record Trade (int tradeId, LocalDate startDate, LocalDate endDate, Instant executionTimestamp, int tradeTypeId, List<TradeLeg> tradeLegs) {

}
