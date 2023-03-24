package com.rhe.trading.agg;

import com.rhe.trading.agg.model.etrm.*;
import io.debezium.serde.DebeziumSerdes;
import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import org.apache.kafka.common.serialization.Serde;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.util.Collections;

@ApplicationScoped
public class SerdeProducer {

    @Produces
    Serde<EtrmTransaction> etrmTransactionSerde() {
        Serde<EtrmTransaction> serde = DebeziumSerdes.payloadJson(EtrmTransaction.class);
        serde.configure(Collections.emptyMap(), false);
        return serde;
    }

    @Produces
    Serde<EtrmTradeHeaderKey> etrmTradeHeaderKeySerde() {
        return new ObjectMapperSerde<>(EtrmTradeHeaderKey.class);
    }

    @Produces
    Serde<EtrmTradeHeader> etrmTradeHeaderSerde() {
        Serde<EtrmTradeHeader> serde = DebeziumSerdes.payloadJson(EtrmTradeHeader.class);
        serde.configure(Collections.emptyMap(), false);
        return serde;
    }

    @Produces
    Serde<EtrmTradeLegKey> etrmTradeLegKeySerde() {
        return new ObjectMapperSerde<>(EtrmTradeLegKey.class);
    }

    @Produces
    Serde<EtrmTradeLeg> etrmTradeLegSerde() {
        Serde<EtrmTradeLeg> serde = DebeziumSerdes.payloadJson(EtrmTradeLeg.class);
        serde.configure(Collections.emptyMap(), false);
        return serde;
    }

}
