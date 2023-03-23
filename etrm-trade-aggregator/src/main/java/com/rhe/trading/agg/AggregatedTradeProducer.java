package com.rhe.trading.agg;

import com.rhe.trading.agg.model.etrm.*;
import io.debezium.serde.DebeziumSerdes;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class AggregatedTradeProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AggregatedTradeProducer.class);

    private static final List<String> TRADE_TABLES = List.of("public.trade_header", "public.trade_leg");

    @Produces
    public Topology build() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        Serde<String> etrmTransactionKeySerde = DebeziumSerdes.payloadJson(String.class);
        etrmTransactionKeySerde.configure(Collections.emptyMap(), true);
        Serde<EtrmTransaction> etrmTransactionSerde = DebeziumSerdes.payloadJson(EtrmTransaction.class);
        etrmTransactionSerde.configure(Collections.emptyMap(), false);

        KStream<String, EtrmTransaction> etrmTransactionKStream = streamsBuilder.stream("etrm.transaction", Consumed.with(etrmTransactionKeySerde, etrmTransactionSerde))
                .peek((s, etrmTransaction) -> LOGGER.info("ALL: {}, {}", s, etrmTransaction))
                .filter((s, etrmTransaction) -> etrmTransaction.status().equals("END"))
                .filter((s, etrmTransaction) -> etrmTransaction.dataCollections().stream().filter(dataCollection -> TRADE_TABLES.contains(dataCollection.dataCollection())).count() > 0)
                .peek((s, etrmTransaction) -> LOGGER.info("FILTERED: {}", etrmTransaction));

        Serde<Integer> integerSerde = DebeziumSerdes.payloadJson(Integer.class);
        integerSerde.configure(Collections.emptyMap(), true);
        Serde<EtrmTradeHeaderEnvelope> etrmTradeHeaderEnvelopeSerde = DebeziumSerdes.payloadJson(EtrmTradeHeaderEnvelope.class);
        etrmTradeHeaderEnvelopeSerde.configure(Collections.singletonMap("unknown.properties.ignored", true), false);

        KStream<Integer, EtrmTradeHeaderEnvelope> etrmTradeHeaderEnvelopeKStream = streamsBuilder.stream("etrm.public.trade_header", Consumed.with(integerSerde, etrmTradeHeaderEnvelopeSerde))
                .peek((key, envelope) -> LOGGER.info("{}, {}", key, envelope));

        Serde<EtrmTradeLegKey> etrmTradeLegKeySerde = DebeziumSerdes.payloadJson(EtrmTradeLegKey.class);
        etrmTradeLegKeySerde.configure(Collections.emptyMap(), true);
        Serde<EtrmTradeLegEnvelope> etrmTradeLegEnvelopeSerde = DebeziumSerdes.payloadJson(EtrmTradeLegEnvelope.class);
        etrmTradeLegEnvelopeSerde.configure(Collections.singletonMap("unknown.properties.ignored", true), false);

        KStream<EtrmTradeLegKey, EtrmTradeLegEnvelope> etrmTradeLegEnvelopeKStream = streamsBuilder.stream("etrm.public.trade_leg", Consumed.with(etrmTradeLegKeySerde, etrmTradeLegEnvelopeSerde))
                .peek((key, envelope) -> LOGGER.info("{}, {}", key, envelope));

        return streamsBuilder.build();
    }


    /*
    @Produces
    public Topology buildTopology() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        Serde<Integer> integerSerde = DebeziumSerdes.payloadJson(Integer.class);
        integerSerde.configure(Collections.emptyMap(), true);
        Serde<EtrmTradeHeaderEnvelope> etrmTradeHeaderEnvelopeSerde = DebeziumSerdes.payloadJson(EtrmTradeHeaderEnvelope.class);
        etrmTradeHeaderEnvelopeSerde.configure(Collections.singletonMap("unknown.properties.ignored", true), false);

        Serde<Integer> etrmTradeLegKeySerde = DebeziumSerdes.payloadJson(Integer.class);
        etrmTradeLegKeySerde.configure(Collections.emptyMap(), true);
        Serde<EtrmTradeLeg> etrmTradeLegSerde = DebeziumSerdes.payloadJson(EtrmTradeLeg.class);
        etrmTradeLegSerde.configure(Collections.singletonMap("from.field", "after"), false);

        Serde<String> etrmTransactionKeySerde = DebeziumSerdes.payloadJson(String.class);
        etrmTransactionKeySerde.configure(Collections.emptyMap(), true);
        Serde<EtrmTransaction> etrmTransactionSerde = DebeziumSerdes.payloadJson(EtrmTransaction.class);
        etrmTransactionSerde.configure(Collections.emptyMap(), false);

        KStream<String, EtrmTransaction> etrmTransactionKStream = streamsBuilder.stream("etrm.transaction", Consumed.with(etrmTransactionKeySerde, etrmTransactionSerde));
        KTable<Integer, EtrmTradeHeaderEnvelope> etrmTradeHeaderKTable = streamsBuilder.table("etrm.public.trade_header", Consumed.with(integerSerde, etrmTradeHeaderEnvelopeSerde));
        KTable<Integer, EtrmTradeLeg> etrmTradeLegKTable = streamsBuilder.table("etrm.public.trade_leg", Consumed.with(etrmTradeLegKeySerde, etrmTradeLegSerde));

        etrmTransactionKStream.peek((str, etrmTransaction) -> LOGGER.info("TXN: {}, {}", str, etrmTransaction));
        etrmTradeHeaderKTable.toStream().peek((integer, etrmTradeHeader) -> LOGGER.info("HEADER: {}, {}", integer, etrmTradeHeader));
        etrmTradeLegKTable.toStream().peek((integer, etrmTradeLeg) -> LOGGER.info("LEG: {}, {}", integer, etrmTradeLeg));

        etrmTransactionKStream
                .filter((s, etrmTransaction) -> etrmTransaction.getStatus().equals("END"))
                .filter((s, etrmTransaction) -> etrmTransaction.getDataCollections().stream().filter(dataCollection -> TRADE_TABLES.contains(dataCollection.getDataCollection())).count() > 0)
                .peek((s, etrmTransaction) -> LOGGER.info("FILTERED: {}", etrmTransaction));



        return streamsBuilder.build();
    }
     */

}
