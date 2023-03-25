package com.rhe.trading.agg;

import com.rhe.trading.agg.model.canonical.Trade;
import com.rhe.trading.agg.model.canonical.TradeLeg;
import com.rhe.trading.agg.model.etrm.*;
import io.debezium.data.Envelope;
import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class TopologyProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopologyProducer.class);

    private final String TOPIC_ETRM_TRANSACTION = "etrm.transaction";
    private final String TOPIC_ETRM_TRADE_HEADER = "etrm.public.trade_header";
    private final String TOPIC_ETRM_TRADE_LEG = "etrm.public.trade_leg";
    private final List<String> TRADE_TABLES = List.of("public.trade_header", "public.trade_leg");

    private final Serde<EtrmTransaction> etrmTransactionSerde;
    private final Serde<EtrmTradeHeaderKey> etrmTradeHeaderKeySerde;
    private final Serde<EtrmTradeHeader> etrmTradeHeaderSerde;
    private final Serde<EtrmTradeLegKey> etrmTradeLegKeySerde;
    private final Serde<EtrmTradeLeg> etrmTradeLegSerde;

    public TopologyProducer(Serde<EtrmTransaction> etrmTransactionSerde,
                            Serde<EtrmTradeHeaderKey> etrmTradeHeaderKeySerde, Serde<EtrmTradeHeader> etrmTradeHeaderSerde,
                            Serde<EtrmTradeLegKey> etrmTradeLegKeySerde, Serde<EtrmTradeLeg> etrmTradeLegSerde) {
        this.etrmTransactionSerde = etrmTransactionSerde;
        this.etrmTradeHeaderKeySerde = etrmTradeHeaderKeySerde;
        this.etrmTradeHeaderSerde = etrmTradeHeaderSerde;
        this.etrmTradeLegKeySerde = etrmTradeLegKeySerde;
        this.etrmTradeLegSerde = etrmTradeLegSerde;
    }

    @Produces
    public Topology produce() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        KStream<String, EtrmTransaction> etrmTransactionKStream = streamsBuilder.stream(TOPIC_ETRM_TRANSACTION, Consumed.with(Serdes.String(), etrmTransactionSerde))
                .peek((k, v) -> LOGGER.debug("ALLTXN: {}, {}", k, v))
                .filter((k, v) -> v.status().equals("END"))
                .filter((k, v) -> v.dataCollections().stream().filter(dc -> TRADE_TABLES.contains(dc.dataCollection())).count() > 0)
                .peek((k, v) -> LOGGER.debug("FILTXN: {}, {}", k, v));

        KTable<Integer, EtrmTradeHeader> etrmTradeHeaderKTable = streamsBuilder.stream(TOPIC_ETRM_TRADE_HEADER, Consumed.with(etrmTradeHeaderKeySerde, etrmTradeHeaderSerde).withOffsetResetPolicy(Topology.AutoOffsetReset.EARLIEST))
                .map((k, v) -> KeyValue.pair(k.tradeId(), v))
                .peek((k, v) -> LOGGER.debug("HEADER: {}, {}", k, v))
                .toTable(Materialized.as("etrm-trade-header-table").with(Serdes.Integer(), etrmTradeHeaderSerde));

        KTable<Integer, EtrmTradeLeg> etrmTradeLegKTable = streamsBuilder.stream(TOPIC_ETRM_TRADE_LEG, Consumed.with(etrmTradeLegKeySerde, etrmTradeLegSerde).withOffsetResetPolicy(Topology.AutoOffsetReset.EARLIEST))
                .map((k, v) -> KeyValue.pair(k.tradeLegId(), v))
                .peek((k, v) -> LOGGER.debug("LEG: {}, {}", k, v))
                .toTable(Materialized.as("etrm-trade-leg-table").with(Serdes.Integer(), etrmTradeLegSerde));

        Serde<EtrmTradeLegs> etrmTradeLegsSerde = new ObjectMapperSerde<>(EtrmTradeLegs.class);
        KTable<Integer, EtrmTradeLegs> etrmTradeLegsKTable = etrmTradeLegKTable.toStream()
                .map((integer, etrmTradeLeg) -> KeyValue.pair(etrmTradeLeg.tradeId(), etrmTradeLeg))
                .groupByKey(Grouped.with(Serdes.Integer(), etrmTradeLegSerde))
                .aggregate(
                        () -> new EtrmTradeLegs(),
                        (integer, etrmTradeLeg, etrmTradeLegs) -> etrmTradeLegs.update(etrmTradeLeg),
                        Materialized.<Integer, EtrmTradeLegs, KeyValueStore<Bytes, byte[]>>
                                        as("etrm-trade-legs-table")
                                .withKeySerde(Serdes.Integer())
                                .withValueSerde(etrmTradeLegsSerde)
                );
        etrmTradeLegsKTable.toStream().peek((k, v) -> LOGGER.debug("LEGAGG: {}, {}", k, v));

        Serde<Trade> tradeSerde = new ObjectMapperSerde<>(Trade.class);
        etrmTradeHeaderKTable.join(
                        etrmTradeLegsKTable,
                        (etrmTradeHeader, etrmTradeLegs) -> this.createTrade(etrmTradeHeader, etrmTradeLegs),
                        Materialized.<Integer, Trade, KeyValueStore<Bytes, byte[]>>
                                        as("trade-table")
                                .withKeySerde(Serdes.Integer())
                                .withValueSerde(tradeSerde)
                ).toStream()
                .peek((k, v) -> LOGGER.debug("TRADE: {}, {}", k, v)).to("trade", Produced.with(Serdes.Integer(), tradeSerde));

        return streamsBuilder.build();
    }

    private Trade createTrade(EtrmTradeHeader etrmTradeHeader, EtrmTradeLegs etrmTradeLegs) {
        if (etrmTradeHeader.op().equals(Envelope.Operation.DELETE.code())) {
            return null;
        }
        List<TradeLeg> tradeLegs = etrmTradeLegs.getEtrmTradeLegs().stream().map(this::createTradeLeg).collect(Collectors.toList());
        return new Trade(etrmTradeHeader.tradeId(), LocalDate.ofEpochDay(etrmTradeHeader.startDate()), LocalDate.ofEpochDay(etrmTradeHeader.endDate()),
                Instant.ofEpochMilli(etrmTradeHeader.executionTimestamp()), etrmTradeHeader.tradeTypeId(), tradeLegs);
    }

    private TradeLeg createTradeLeg(EtrmTradeLeg etrmTradeLeg) {
        return new TradeLeg(etrmTradeLeg.tradeLegId(), etrmTradeLeg.tradeId(), etrmTradeLeg.payerId(), etrmTradeLeg.receiverId(),
                etrmTradeLeg.commodity_id(), etrmTradeLeg.locationId(), etrmTradeLeg.price(), etrmTradeLeg.priceCurrencyId(),
                etrmTradeLeg.quantity(), etrmTradeLeg.quantityUomId());
    }

}
