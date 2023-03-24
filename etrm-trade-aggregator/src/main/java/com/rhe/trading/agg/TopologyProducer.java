package com.rhe.trading.agg;

import com.rhe.trading.agg.model.canonical.Trade;
import com.rhe.trading.agg.model.canonical.TradeLeg;
import com.rhe.trading.agg.model.etrm.*;
import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
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
import java.util.stream.Collectors;

@ApplicationScoped
public class TopologyProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopologyProducer.class);

    private final Serde<EtrmTradeHeaderKey> etrmTradeHeaderKeySerde;
    private final Serde<EtrmTradeHeader> etrmTradeHeaderSerde;
    private final Serde<EtrmTradeLegKey> etrmTradeLegKeySerde;
    private final Serde<EtrmTradeLeg> etrmTradeLegSerde;

    public TopologyProducer(Serde<EtrmTradeHeaderKey> etrmTradeHeaderKeySerde, Serde<EtrmTradeHeader> etrmTradeHeaderSerde, Serde<EtrmTradeLegKey> etrmTradeLegKeySerde, Serde<EtrmTradeLeg> etrmTradeLegSerde) {
        this.etrmTradeHeaderKeySerde = etrmTradeHeaderKeySerde;
        this.etrmTradeHeaderSerde = etrmTradeHeaderSerde;
        this.etrmTradeLegKeySerde = etrmTradeLegKeySerde;
        this.etrmTradeLegSerde = etrmTradeLegSerde;
    }

    @Produces
    public Topology produce() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        KStream<Integer, EtrmTradeHeader> etrmTradeHeaderKStream = streamsBuilder.stream("etrm.public.trade_header", Consumed.with(etrmTradeHeaderKeySerde, etrmTradeHeaderSerde))
                .map((k, v) -> new KeyValue<>(k.tradeId(), v))
                .peek((k, v) -> LOGGER.info("HEADER: {}, {}", k, v));


        KStream<Integer, EtrmTradeLeg> etrmTradeLegKStream = streamsBuilder.stream("etrm.public.trade_leg", Consumed.with(etrmTradeLegKeySerde, etrmTradeLegSerde))
                .map((k, v) -> new KeyValue<>(k.tradeLegId(), v))
                .peek((k, v) -> LOGGER.info("LEG: {}, {}", k, v));

        Serde<EtrmTradeLegs> etrmTradeLegsSerde = new ObjectMapperSerde<>(EtrmTradeLegs.class);

        KGroupedTable<Integer, EtrmTradeLeg> etrmTradeLegKGroupedTable = etrmTradeLegKStream.toTable(Materialized.with(Serdes.Integer(), etrmTradeLegSerde))
                .groupBy((integer, etrmTradeLeg) -> new KeyValue<>(etrmTradeLeg.tradeId(), etrmTradeLeg), Grouped.with(Serdes.Integer(), etrmTradeLegSerde));

        KTable<Integer, EtrmTradeLegs> agg = etrmTradeLegKGroupedTable.aggregate(
                () -> new EtrmTradeLegs(),
                (key, value, aggregate) -> aggregate.update(value),
                (key, value, aggregate) -> aggregate.update(value),
                Materialized.with(Serdes.Integer(), etrmTradeLegsSerde)
        );
        agg.toStream().peek((k, v) -> LOGGER.info("AGG: {}, {}", k, v));

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

    /*
    Serde<TradeIdAndTransactionId> tradeIdAndTransactionIdSerde = new ObjectMapperSerde(TradeIdAndTransactionId.class);
        Serde<EtrmTradeHeader> etrmTradeHeaderSerde = new ObjectMapperSerde(EtrmTradeHeader.class);

        KTable<TradeIdAndTransactionId, EtrmTradeHeader> etrmTradeHeaderEnvelopeKTable = etrmTradeHeaderEnvelopeKStream
                .map((integer, etrmTradeHeaderEnvelope) -> {
                    int tradeId = etrmTradeHeaderEnvelope.getAfter().tradeId();
                    String transactionIdString = etrmTradeHeaderEnvelope.getTransaction().id();
                    int transactionId = Integer.parseInt(transactionIdString.substring(0, transactionIdString.indexOf(':')));
                    TradeIdAndTransactionId tradeIdAndTransactionId = new TradeIdAndTransactionId(tradeId, transactionId);
                    return new KeyValue<>(tradeIdAndTransactionId, etrmTradeHeaderEnvelope.getAfter());
                })
                .toTable(Materialized.with(tradeIdAndTransactionIdSerde, etrmTradeHeaderSerde));

        etrmTradeHeaderEnvelopeKTable.toStream().peek((key, envelope) -> LOGGER.info("TABLE: {}, {}", key, envelope));

        Serde<EtrmTradeLegAggregation> etrmTradeLegAggregationSerde = new ObjectMapperSerde(EtrmTradeLegAggregation.class);
        Serde<EtrmTradeLeg> etrmTradeLegSerde = new ObjectMapperSerde<>(EtrmTradeLeg.class);

        KGroupedStream<Integer, EtrmTradeLeg> etrmTradeLegKeyEtrmTradeLegEnvelopeKGroupedStream = etrmTradeLegEnvelopeKStream
                .map((etrmTradeLegKey, etrmTradeLegEnvelope) -> new KeyValue<>(etrmTradeLegKey.tradeId(), etrmTradeLegEnvelope.getAfter()))
                .groupByKey(Grouped.with(Serdes.Integer(), etrmTradeLegSerde));

        KTable<Integer, EtrmTradeLegAggregation> aggregate =

        aggregate.toStream().peek((key, aggregation) -> LOGGER.info("AGG: {}, {}", key, aggregation));
     */

    /*
    Serde<EtrmTradeLeg> etrmTradeLegSerde = new ObjectMapperSerde<>(EtrmTradeLeg.class);
        Serde<EtrmTradeLegAggregation> etrmTradeLegAggregationSerde = new ObjectMapperSerde(EtrmTradeLegAggregation.class);

        KGroupedStream<Integer, EtrmTradeLeg> etrmTradeLegKeyEtrmTradeLegEnvelopeKGroupedStream = etrmTradeLegEnvelopeKStream
                .map((etrmTradeLegKey, etrmTradeLegEnvelope) -> new KeyValue<>(etrmTradeLegKey.tradeId(), etrmTradeLegEnvelope.getAfter()))
                .groupByKey(Grouped.with(Serdes.Integer(), etrmTradeLegSerde));
        KTable<Integer, EtrmTradeLegAggregation> etrmTradeLegAggregationKTable = etrmTradeLegKeyEtrmTradeLegEnvelopeKGroupedStream.aggregate(
                () -> new EtrmTradeLegAggregation(new ArrayList<>()),
                (integer, etrmTradeLeg, aggregation) -> {
                    aggregation.tradeLegs().add(etrmTradeLeg);
                    return aggregation;
                },
                Materialized.with(Serdes.Integer(), etrmTradeLegAggregationSerde));
        etrmTradeLegAggregationKTable.toStream().peek((key, aggregation) -> LOGGER.info("etrmTradeLegAggregationKTable: {}, {}", key, aggregation));
     */

    /*

        KStream<String, EtrmTransaction> etrmTransactionKStream = streamsBuilder.stream("etrm.transaction", Consumed.with(etrmTransactionKeySerde, etrmTransactionSerde))
                .peek((s, etrmTransaction) -> LOGGER.info("ALL TRANSACTIONS: {}, {}", s, etrmTransaction))
                .filter((s, etrmTransaction) -> etrmTransaction.status().equals("END"))
                .filter((s, etrmTransaction) -> etrmTransaction.dataCollections().stream().filter(dataCollection -> TRADE_TABLES.contains(dataCollection.dataCollection())).count() > 0)
                .peek((s, etrmTransaction) -> LOGGER.info("FILTERED TRANSACTIONS: {}", etrmTransaction));

        KStream<Integer, EtrmTradeHeaderEnvelope> etrmTradeHeaderEnvelopeKStream = streamsBuilder.stream("etrm.public.trade_header", Consumed.with(integerSerde, etrmTradeHeaderEnvelopeSerde))
                .peek((key, envelope) -> LOGGER.info("ETRMTRADEHEADERENVELOPEKSTREAM: {}, {}", key, envelope));

        KStream<Integer, EtrmTradeLegEnvelope> etrmTradeLegEnvelopeKStream = streamsBuilder.stream("etrm.public.trade_leg", Consumed.with(integerSerde, etrmTradeLegEnvelopeSerde))
                .peek((key, envelope) -> LOGGER.info("ETRMTRADELEGENVELOPEKSTREAM: {}, {}", key, envelope));

        // NEW STUFF
        Serde<EtrmTradeLeg> etrmTradeLegSerde = new ObjectMapperSerde<>(EtrmTradeLeg.class);
        Serde<EtrmTradeLegAggregation> etrmTradeLegAggregationSerde = new ObjectMapperSerde<>(EtrmTradeLegAggregation.class);
        KTable<Integer, EtrmTradeLegAggregation> etrmTradeLegAggregationKTable = etrmTradeLegEnvelopeKStream
                .map((integer, etrmTradeLegEnvelope) -> new KeyValue<>(this.getTradeId(etrmTradeLegEnvelope), etrmTradeLegEnvelope))
                .groupByKey(Grouped.with(Serdes.Integer(), etrmTradeLegEnvelopeSerde))
                .aggregate(
                        EtrmTradeLegAggregation::new,
                        (integer, etrmTradeLegEnvelope, aggregation) -> aggregation.update(etrmTradeLegEnvelope),
                        Materialized.with(Serdes.Integer(), etrmTradeLegAggregationSerde)
                );
        etrmTradeLegAggregationKTable.toStream().peek((k, v) -> LOGGER.info("TradeLegAggregation: {}, {}", k, v));

     */

}
