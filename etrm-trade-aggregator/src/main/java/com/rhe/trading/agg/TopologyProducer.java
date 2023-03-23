package com.rhe.trading.agg;

import com.rhe.trading.agg.model.etrm.EtrmTradeHeader;
import com.rhe.trading.agg.model.etrm.EtrmTradeHeaderKey;
import com.rhe.trading.agg.model.etrm.EtrmTradeLeg;
import com.rhe.trading.agg.model.etrm.EtrmTradeLegKey;
import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

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

        KTable<EtrmTradeHeaderKey, EtrmTradeHeader> etrmTradeHeaderKTable = streamsBuilder.table("etrm.public.trade_header", Consumed.with(etrmTradeHeaderKeySerde, etrmTradeHeaderSerde));
        etrmTradeHeaderKTable.toStream().peek((k, v) -> LOGGER.info("{}, {}", k, v));

        KTable<EtrmTradeLegKey, EtrmTradeLeg> etrmTradeLegKTable = streamsBuilder.table("etrm.public.trade_leg", Consumed.with(etrmTradeLegKeySerde, etrmTradeLegSerde));
        etrmTradeLegKTable.toStream().peek((k, v) -> LOGGER.info("{}, {}", k, v));

        Serde<EtrmTradeLegAndEtrmTradeHeader> etrmTradeLegAndEtrmTradeHeaderSerde = new ObjectMapperSerde<>(EtrmTradeLegAndEtrmTradeHeader.class);

        return streamsBuilder.build();
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
