package com.rhe.trading.agg;

import com.rhe.trading.agg.model.etrm.EtrmTradeHeader;
import com.rhe.trading.agg.model.etrm.EtrmTradeLeg;
import com.rhe.trading.agg.model.etrm.EtrmTradeLegKey;
import com.rhe.trading.agg.model.etrm.EtrmTransaction;
import io.debezium.data.Envelope;
import io.debezium.data.SchemaUtil;
import io.debezium.serde.DebeziumSerdes;
import org.apache.kafka.common.protocol.types.Schema;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.transaction.Status;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class TradeTopologyProducer {

    public static final Logger LOGGER = LoggerFactory.getLogger(TradeTopologyProducer.class);

    private static final List<String> TRADE_TABLES = List.of("public.trade_header", "public.trade_leg");

    @Produces
    public Topology buildTopology() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KTable<Integer, EtrmTradeHeader> etrmTradeHeaderKTable = this.createEtrmTradeHeaderKTable(streamsBuilder);
        KTable<EtrmTradeLegKey, EtrmTradeLeg> etrmTradeLegKTable = this.createEtrmTradeLegKTable(streamsBuilder);
        KStream<String, EtrmTransaction> etrmTransactionKStream = this.createEtrmTransactionKStream(streamsBuilder);
        etrmTransactionKStream.peek((etrmTransactionKey, etrmTransaction) -> LOGGER.info("ALL: {}", etrmTransaction));
        etrmTransactionKStream
                .filter((s, etrmTransaction) -> etrmTransaction.getStatus().equals("END"))
                .filter((s, etrmTransaction) -> etrmTransaction.getDataCollections().stream().filter(dataCollection -> TRADE_TABLES.contains(dataCollection.getDataCollection())).count() > 0)
                .peek((s, etrmTransaction) -> LOGGER.info("FILTERED: {}", etrmTransaction));

        return streamsBuilder.build();
    }

    private KTable<Integer, EtrmTradeHeader> createEtrmTradeHeaderKTable(StreamsBuilder streamsBuilder) {
        Serde<Integer> keySerde = DebeziumSerdes.payloadJson(Integer.class);
        keySerde.configure(Collections.emptyMap(), true);
        Serde<EtrmTradeHeader> valueSerde = DebeziumSerdes.payloadJson(EtrmTradeHeader.class);
        valueSerde.configure(Collections.singletonMap("from.field", "after"), false);
        KTable<Integer, EtrmTradeHeader> kTable = streamsBuilder.table("etrm.public.trade_header", Consumed.with(keySerde, valueSerde));
        return kTable;
    }

    private KTable<EtrmTradeLegKey, EtrmTradeLeg> createEtrmTradeLegKTable(StreamsBuilder streamsBuilder) {
        Serde<EtrmTradeLegKey> keySerde = DebeziumSerdes.payloadJson(EtrmTradeLegKey.class);
        keySerde.configure(Collections.emptyMap(), true);
        Serde<EtrmTradeLeg> valueSerde = DebeziumSerdes.payloadJson(EtrmTradeLeg.class);
        valueSerde.configure(Collections.singletonMap("from.field", "after"), false);
        KTable<EtrmTradeLegKey, EtrmTradeLeg> kTable = streamsBuilder.table("etrm.public.trade_leg", Consumed.with(keySerde, valueSerde));
        return kTable;
    }

    private KStream<String, EtrmTransaction> createEtrmTransactionKStream(StreamsBuilder streamsBuilder) {
        Serde<String> etrmTransactionKeySerde = DebeziumSerdes.payloadJson(String.class);
        etrmTransactionKeySerde.configure(Collections.emptyMap(), true);
        Serde<EtrmTransaction> etrmTransactionSerde = DebeziumSerdes.payloadJson(EtrmTransaction.class);
        etrmTransactionSerde.configure(Collections.emptyMap(), false);
        KStream<String, EtrmTransaction> etrmTransactionKStream = streamsBuilder.stream("etrm.transaction", Consumed.with(etrmTransactionKeySerde, etrmTransactionSerde));
        return etrmTransactionKStream;
    }

}
