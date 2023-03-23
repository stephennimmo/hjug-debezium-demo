package com.rhe.trading.agg;

import com.rhe.trading.agg.model.canonical.Trade;
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
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class TopologyProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopologyProducer.class);

    private static final List<String> TRADE_TABLES = List.of("public.trade_header", "public.trade_leg");

    private final Serde<Integer> integerSerde;
    private final Serde<String> etrmTransactionKeySerde;
    private final Serde<EtrmTransaction> etrmTransactionSerde;
    private final Serde<EtrmTradeHeaderEnvelope> etrmTradeHeaderEnvelopeSerde;
    private final Serde<EtrmTradeLegEnvelope> etrmTradeLegEnvelopeSerde;

    public TopologyProducer(Serde<Integer> integerSerde, Serde<String> etrmTransactionKeySerde, Serde<EtrmTransaction> etrmTransactionSerde, Serde<EtrmTradeHeaderEnvelope> etrmTradeHeaderEnvelopeSerde, Serde<EtrmTradeLegEnvelope> etrmTradeLegEnvelopeSerde) {
        this.integerSerde = integerSerde;
        this.etrmTransactionKeySerde = etrmTransactionKeySerde;
        this.etrmTransactionSerde = etrmTransactionSerde;
        this.etrmTradeHeaderEnvelopeSerde = etrmTradeHeaderEnvelopeSerde;
        this.etrmTradeLegEnvelopeSerde = etrmTradeLegEnvelopeSerde;
    }

    @Produces
    public Topology produce() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();

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

        return streamsBuilder.build();
    }

    private int getTradeId(EtrmTradeLegEnvelope envelope) {
        if (envelope.getAfter() != null) {
            return envelope.getAfter().tradeId();
        } else if (envelope.getBefore() != null) {
            return envelope.getBefore().tradeId();
        }
        throw new RuntimeException("Before and After are null");
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

}
