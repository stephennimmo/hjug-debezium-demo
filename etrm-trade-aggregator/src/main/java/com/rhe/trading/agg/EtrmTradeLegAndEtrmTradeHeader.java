package com.rhe.trading.agg;

import com.rhe.trading.agg.model.etrm.EtrmTradeHeader;
import com.rhe.trading.agg.model.etrm.EtrmTradeLeg;

public record EtrmTradeLegAndEtrmTradeHeader(EtrmTradeLeg etrmTradeLeg, EtrmTradeHeader etrmTradeHeader) {
}
