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

    private final List<String> TRADE_TABLES = List.of("public.trade_header", "public.trade_leg");
    private final String TOPIC_ETRM_TRADE_HEADER = "etrm.public.trade_header";
    private final String TOPIC_ETRM_TRADE_LEG = "etrm.public.trade_leg";

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

        KTable<Integer, EtrmTradeHeader> etrmTradeHeaderKTable = streamsBuilder.stream(TOPIC_ETRM_TRADE_HEADER, Consumed.with(etrmTradeHeaderKeySerde, etrmTradeHeaderSerde).withOffsetResetPolicy(Topology.AutoOffsetReset.EARLIEST))
                .map((k, v) -> KeyValue.pair(k.tradeId(), v))
                .peek((k, v) -> LOGGER.info("HEADER: {}, {}", k, v))
                .toTable(Materialized.as("etrmTradeHeaderKTable").with(Serdes.Integer(), etrmTradeHeaderSerde));

        KTable<Integer, EtrmTradeLeg> etrmTradeLegKTable = streamsBuilder.stream(TOPIC_ETRM_TRADE_LEG, Consumed.with(etrmTradeLegKeySerde, etrmTradeLegSerde).withOffsetResetPolicy(Topology.AutoOffsetReset.EARLIEST))
                .map((k, v) -> KeyValue.pair(k.tradeLegId(), v))
                .peek((k, v) -> LOGGER.info("LEG: {}, {}", k, v))
                .toTable(Materialized.as("etrmTradeLegKTable").with(Serdes.Integer(), etrmTradeLegSerde));

        Serde<TradeAndLeg> tradeAndLegSerde = new ObjectMapperSerde<>(TradeAndLeg.class);
        KTable<Integer, TradeAndLeg> tradeAndLegKTable = etrmTradeLegKTable.toStream().groupByKey(Grouped.with(Serdes.Integer(), etrmTradeLegSerde))
                .aggregate(
                        () -> new TradeAndLeg(),
                        (tradeLegId, etrmTradeLeg, tradeAndLeg) -> tradeAndLeg.update(tradeLegId, etrmTradeLeg.tradeId(), etrmTradeLeg),
                        Materialized.<Integer, TradeAndLeg, KeyValueStore<Bytes, byte[]>>
                                        as("tradeAndLegKTable")
                                .withKeySerde(Serdes.Integer())
                                .withValueSerde(tradeAndLegSerde)
                );

        Serde<EtrmTradeLegs> etrmTradeLegsSerde = new ObjectMapperSerde<>(EtrmTradeLegs.class);
        KTable<Integer, EtrmTradeLegs> etrmTradeLegsKTable = tradeAndLegKTable.toStream()
                .map((key, value) -> KeyValue.pair(value.getTradeId(), value))
                .groupByKey(Grouped.with(Serdes.Integer(), tradeAndLegSerde))
                .aggregate(
                        () -> new EtrmTradeLegs(),
                        (key, value, aggregate) -> aggregate.update(value),
                        Materialized.<Integer, EtrmTradeLegs, KeyValueStore<Bytes, byte[]>>
                                        as("etrmTradeLegsKTable")
                                .withKeySerde(Serdes.Integer())
                                .withValueSerde(etrmTradeLegsSerde)
                );

        Serde<Trade> tradeSerde = new ObjectMapperSerde<>(Trade.class);
        KTable<Integer, Trade> tradeKTable = etrmTradeHeaderKTable.join(etrmTradeLegsKTable, (value1, value2) -> {
            return value1.op().equals(Envelope.Operation.DELETE.code()) ? null : this.createTrade(value1, value2);
        });

        tradeKTable.toStream()
                .peek((k, v) -> LOGGER.info("TRADE: {}, {}", k, v))
                .to("trade", Produced.with(Serdes.Integer(), tradeSerde));

        return streamsBuilder.build();
    }

    private Trade createTrade(EtrmTradeHeader etrmTradeHeader, EtrmTradeLegs etrmTradeLegs) {
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
