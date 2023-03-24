package com.rhe.trading.agg;

import com.rhe.trading.agg.model.canonical.Trade;
import com.rhe.trading.agg.model.canonical.TradeLeg;
import com.rhe.trading.agg.model.etrm.*;
import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.CopyOnWriteMap;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class TopologyProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopologyProducer.class);

    private final List<String> TRADE_TABLES = List.of("public.trade_header", "public.trade_leg");

    private final Serde<EtrmTransaction> etrmTransactionSerde;
    private final Serde<EtrmTradeHeaderKey> etrmTradeHeaderKeySerde;
    private final Serde<EtrmTradeHeader> etrmTradeHeaderSerde;
    private final Serde<EtrmTradeLegKey> etrmTradeLegKeySerde;
    private final Serde<EtrmTradeLeg> etrmTradeLegSerde;

    private final Map<Integer, Integer> tradeLegIdToTradeIdMap = new CopyOnWriteMap<>();

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

        KStream<String, EtrmTransaction> etrmTransactionKStream = streamsBuilder.stream("etrm.transaction", Consumed.with(Serdes.String(), etrmTransactionSerde))
                .peek((s, etrmTransaction) -> LOGGER.info("ALL TRANSACTIONS: {}, {}", s, etrmTransaction))
                .filter((s, etrmTransaction) -> etrmTransaction.status().equals("END"))
                .filter((s, etrmTransaction) -> etrmTransaction.dataCollections().stream().filter(dataCollection -> TRADE_TABLES.contains(dataCollection.dataCollection())).count() > 0)
                .peek((s, etrmTransaction) -> LOGGER.info("FILTERED TRANSACTIONS: {}", etrmTransaction));

        KStream<Integer, EtrmTradeHeader> etrmTradeHeaderKStream = streamsBuilder.stream("etrm.public.trade_header", Consumed.with(etrmTradeHeaderKeySerde, etrmTradeHeaderSerde))
                .map((k, v) -> new KeyValue<>(k.tradeId(), v))
                .peek((k, v) -> LOGGER.info("HEADER: {}, {}", k, v));

        KStream<Integer, EtrmTradeLeg> etrmTradeLegKStream = streamsBuilder.stream("etrm.public.trade_leg", Consumed.with(etrmTradeLegKeySerde, etrmTradeLegSerde))
                .map((k, v) -> {
                    if (v.tradeId() > 0) {
                        LOGGER.info("Added tradeLegId[{}], tradeId[{}] to tradeLegIdToTradeIdMap", v.tradeLegId(), v.tradeId());
                        tradeLegIdToTradeIdMap.put(v.tradeLegId(), v.tradeId());
                    }
                    return new KeyValue<>(k.tradeLegId(), v);
                })
                .peek((k, v) -> LOGGER.info("LEG: {}, {}", k, v));

        Serde<EtrmTradeLegs> etrmTradeLegsSerde = new ObjectMapperSerde<>(EtrmTradeLegs.class);

        KGroupedTable<Integer, EtrmTradeLeg> etrmTradeLegKGroupedTable = etrmTradeLegKStream
                .toTable(Materialized.with(Serdes.Integer(), etrmTradeLegSerde))
                .groupBy((tradeLegId, etrmTradeLeg) -> new KeyValue<>(this.getTradeIdFromMap(tradeLegId), etrmTradeLeg), Grouped.with(Serdes.Integer(), etrmTradeLegSerde));

        KTable<Integer, EtrmTradeLegs> agg = etrmTradeLegKGroupedTable.aggregate(
                () -> new EtrmTradeLegs(),
                (key, value, aggregate) -> aggregate.update(value),
                (key, value, aggregate) -> aggregate.remove(value),
                Materialized.with(Serdes.Integer(), etrmTradeLegsSerde)
        );
        agg.toStream().peek((k, v) -> LOGGER.info("LEGAGG: {}, {}", k, v));

        Serde<Trade> tradeSerde = new ObjectMapperSerde<>(Trade.class);
        KTable<Integer, Trade> tradeKTable = etrmTradeHeaderKStream.toTable(Materialized.with(Serdes.Integer(), etrmTradeHeaderSerde)).join(
                agg,
                (header, legs) -> this.createTrade(header, legs),
                Materialized.with(Serdes.Integer(), tradeSerde)
        );
        tradeKTable.toStream().peek((k, v) -> LOGGER.info("TRADE: {}, {}", k, v));

        tradeKTable.toStream().to("trade", Produced.with(Serdes.Integer(), tradeSerde));

        return streamsBuilder.build();
    }

    private int getTradeIdFromMap(int tradeLegId) {
        int tradeId = this.tradeLegIdToTradeIdMap.get(tradeLegId);
        LOGGER.info("Got tradeId[{}] from tradeLegIdToTradeIdMap for tradeLegId[{}]", tradeId, tradeLegId);
        return tradeId;
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
