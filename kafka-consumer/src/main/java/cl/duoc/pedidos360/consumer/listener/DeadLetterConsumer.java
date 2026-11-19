package cl.duoc.pedidos360.consumer.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class DeadLetterConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterConsumer.class);

    @KafkaListener(
            topics = {
                    "${app.kafka.topics.pedidos-stream-dlt:pedidos.streaming.eventos.DLT}",
                    "${app.kafka.topics.pedidos-audit-dlt:pedidos.auditoria.log.DLT}"
            },
            groupId = "pedidos360-dlt-monitor-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onDeadLetterMessage(
            @Payload(required = false) Object payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key
    ) {
        log.error("💀 [DLT ALERTA] Mensaje en cola muerta '{}' [Partición: {}, Offset: {}, Key: '{}']. Payload: {}",
                topic, partition, offset, key, payload);
    }
}
