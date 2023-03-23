package com.rhe.trading.agg;

import com.rhe.trading.agg.model.etrm.*;
import io.debezium.serde.DebeziumSerdes;
import io.quarkus.kafka.client.serialization.JsonbSerde;
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
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class AggregatedTradeProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AggregatedTradeProducer.class);

    private static final List<String> TRADE_TABLES = List.of("public.trade_header", "public.trade_leg");

    private final Serde<String> etrmTransactionKeySerde;
    private final Serde<EtrmTransaction> etrmTransactionSerde;
    private final Serde<Integer> etrmTradeHeaderEnvelopeKeySerde;
    private final Serde<EtrmTradeHeaderEnvelope> etrmTradeHeaderEnvelopeSerde;
    private final Serde<EtrmTradeLegKey> etrmTradeLegKeySerde;
    private final Serde<EtrmTradeLegEnvelope> etrmTradeLegEnvelopeSerde;

    public AggregatedTradeProducer(Serde<String> etrmTransactionKeySerde, Serde<EtrmTransaction> etrmTransactionSerde, Serde<Integer> etrmTradeHeaderEnvelopeKeySerde, Serde<EtrmTradeHeaderEnvelope> etrmTradeHeaderEnvelopeSerde, Serde<EtrmTradeLegKey> etrmTradeLegKeySerde, Serde<EtrmTradeLegEnvelope> etrmTradeLegEnvelopeSerde) {
        this.etrmTransactionKeySerde = etrmTransactionKeySerde;
        this.etrmTransactionSerde = etrmTransactionSerde;
        this.etrmTradeHeaderEnvelopeKeySerde = etrmTradeHeaderEnvelopeKeySerde;
        this.etrmTradeHeaderEnvelopeSerde = etrmTradeHeaderEnvelopeSerde;
        this.etrmTradeLegKeySerde = etrmTradeLegKeySerde;
        this.etrmTradeLegEnvelopeSerde = etrmTradeLegEnvelopeSerde;
    }

    @Produces
    public Topology build() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        KStream<String, EtrmTransaction> etrmTransactionKStream = streamsBuilder.stream("etrm.transaction", Consumed.with(etrmTransactionKeySerde, etrmTransactionSerde))
                .peek((s, etrmTransaction) -> LOGGER.info("ALL: {}, {}", s, etrmTransaction))
                .filter((s, etrmTransaction) -> etrmTransaction.status().equals("END"))
                .filter((s, etrmTransaction) -> etrmTransaction.dataCollections().stream().filter(dataCollection -> TRADE_TABLES.contains(dataCollection.dataCollection())).count() > 0)
                .peek((s, etrmTransaction) -> LOGGER.info("FILTERED: {}", etrmTransaction));

        KStream<Integer, EtrmTradeHeaderEnvelope> etrmTradeHeaderEnvelopeKStream = streamsBuilder.stream("etrm.public.trade_header", Consumed.with(etrmTradeHeaderEnvelopeKeySerde, etrmTradeHeaderEnvelopeSerde))
                .peek((key, envelope) -> LOGGER.info("{}, {}", key, envelope));

        KStream<EtrmTradeLegKey, EtrmTradeLegEnvelope> etrmTradeLegEnvelopeKStream = streamsBuilder.stream("etrm.public.trade_leg", Consumed.with(etrmTradeLegKeySerde, etrmTradeLegEnvelopeSerde))
                .peek((key, envelope) -> LOGGER.info("{}, {}", key, envelope));

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
        KGroupedStream<EtrmTradeLegKey, EtrmTradeLegEnvelope> etrmTradeLegKeyEtrmTradeLegEnvelopeKGroupedStream = etrmTradeLegEnvelopeKStream.groupByKey();
        KTable<EtrmTradeLegKey, EtrmTradeLegAggregation> aggregate = etrmTradeLegKeyEtrmTradeLegEnvelopeKGroupedStream.aggregate(
                () -> new EtrmTradeLegAggregation(new ArrayList<>()),
                (etrmTradeLegKey, etrmTradeLegEnvelope, etrmTradeLegAggregation) -> {
                    etrmTradeLegAggregation.tradeLegs().add(etrmTradeLegEnvelope.getAfter());
                    return etrmTradeLegAggregation;
                },
                Materialized.with(etrmTradeLegKeySerde, etrmTradeLegAggregationSerde));

        aggregate.toStream().peek((key, aggregation) -> LOGGER.info("AGG: {}, {}", key, aggregation));

        return streamsBuilder.build();
    }

}
