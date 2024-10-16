package com.dylibso.examples.kafka;

import com.dylibso.examples.kafka.transforms.KafkaTransformStore;
import com.dylibso.examples.viz.VizSocket;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Multi;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import java.io.IOException;

@ApplicationScoped
public class Processor {
    @Inject
    ObjectMapper mapper;

    @Inject
    VizSocket socket;

    @Inject
    KafkaTransformStore transforms;

    @Incoming("pricing-data")
    @Outgoing("processed-price")
    Multi<Message<byte[]>> read(KafkaRecord<byte[], byte[]> pricingData) throws IOException {
        var r = Record.of(pricingData, mapper);
        socket.onRecord(r);
        return transforms.transform(r, mapper)
                .invoke(socket::onRecord)
                .map(rec -> rec.toOutgoingKafkaRecord(mapper))
                .onTermination().invoke(pricingData::ack);
    }
}
