package com.rhe.trading.agg;

import com.rhe.trading.agg.model.etrm.EtrmTradeHeaderEnvelope;
import com.rhe.trading.agg.model.etrm.EtrmTradeLegEnvelope;
import com.rhe.trading.agg.model.etrm.EtrmTradeLegKey;
import com.rhe.trading.agg.model.etrm.EtrmTransaction;
import io.debezium.serde.DebeziumSerdes;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.kstream.KStream;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.util.Collections;

@ApplicationScoped
public class SerdeStreamTableProducer {

    @Produces
    Serde<String> etrmTransactionKeySerde() {
        Serde<String> etrmTransactionKeySerde = DebeziumSerdes.payloadJson(String.class);
        etrmTransactionKeySerde.configure(Collections.emptyMap(), true);
        return etrmTransactionKeySerde;
    }

    @Produces
    Serde<EtrmTransaction> etrmTransactionSerde() {
        Serde<EtrmTransaction> etrmTransactionSerde = DebeziumSerdes.payloadJson(EtrmTransaction.class);
        etrmTransactionSerde.configure(Collections.emptyMap(), false);
        return etrmTransactionSerde;
    }

    @Produces
    Serde<Integer> etrmTradeHeaderEnvelopeKeySerde() {
        Serde<Integer> integerSerde = DebeziumSerdes.payloadJson(Integer.class);
        integerSerde.configure(Collections.emptyMap(), true);
        return integerSerde;
    }

    @Produces
    Serde<EtrmTradeHeaderEnvelope> etrmTradeHeaderEnvelopeSerde() {
        Serde<EtrmTradeHeaderEnvelope> etrmTradeHeaderEnvelopeSerde = DebeziumSerdes.payloadJson(EtrmTradeHeaderEnvelope.class);
        etrmTradeHeaderEnvelopeSerde.configure(Collections.singletonMap("unknown.properties.ignored", true), false);
        return etrmTradeHeaderEnvelopeSerde;
    }

    @Produces
    Serde<EtrmTradeLegKey> etrmTradeLegKeySerde() {
        Serde<EtrmTradeLegKey> etrmTradeLegKeySerde = DebeziumSerdes.payloadJson(EtrmTradeLegKey.class);
        etrmTradeLegKeySerde.configure(Collections.emptyMap(), true);
        return etrmTradeLegKeySerde;
    }

    @Produces
    Serde<EtrmTradeLegEnvelope> etrmTradeLegEnvelopeSerde() {
        Serde<EtrmTradeLegEnvelope> etrmTradeLegEnvelopeSerde = DebeziumSerdes.payloadJson(EtrmTradeLegEnvelope.class);
        etrmTradeLegEnvelopeSerde.configure(Collections.singletonMap("unknown.properties.ignored", true), false);
        return etrmTradeLegEnvelopeSerde;
    }

}
