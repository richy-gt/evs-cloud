package cl.duoc.pedidos360.consumer.listener;

import cl.duoc.pedidos360.consumer.service.AnalyticsProcessingService;
import cl.duoc.pedidos360.kafka.dto.PedidoEventKafkaDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class PedidoStreamConsumer {

    private static final Logger log = LoggerFactory.getLogger(PedidoStreamConsumer.class);

    private final AnalyticsProcessingService analyticsProcessingService;

    public PedidoStreamConsumer(AnalyticsProcessingService analyticsProcessingService) {
        this.analyticsProcessingService = analyticsProcessingService;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.pedidos-stream:pedidos.streaming.eventos}",
            groupId = "${app.kafka.groups.analytics:pedidos360-analytics-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onPedidoEvent(
            @Payload PedidoEventKafkaDto event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key
    ) {
        log.info("📥 [STREAM CONSUMER] Evento recibido desde topic 'pedidos.streaming.eventos' [Partición: {}, Offset: {}, Key: '{}']",
                partition, offset, key);

        // Delegar la lógica de negocio al servicio de dominio correspondiente
        analyticsProcessingService.procesarEventoPedido(event);
    }
}
