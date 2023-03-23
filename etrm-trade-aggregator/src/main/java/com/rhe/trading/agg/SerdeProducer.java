package com.rhe.trading.agg;

import com.rhe.trading.agg.model.etrm.EtrmTradeHeader;
import com.rhe.trading.agg.model.etrm.EtrmTradeHeaderKey;
import com.rhe.trading.agg.model.etrm.EtrmTradeLeg;
import com.rhe.trading.agg.model.etrm.EtrmTradeLegKey;
import io.debezium.serde.DebeziumSerdes;
import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import org.apache.kafka.common.serialization.Serde;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.util.Collections;

@ApplicationScoped
public class SerdeProducer {

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
