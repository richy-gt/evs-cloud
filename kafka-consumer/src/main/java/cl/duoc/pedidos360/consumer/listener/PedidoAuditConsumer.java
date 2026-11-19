package cl.duoc.pedidos360.consumer.listener;

import cl.duoc.pedidos360.consumer.service.AuditProcessingService;
import cl.duoc.pedidos360.kafka.dto.PedidoAuditoriaKafkaDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class PedidoAuditConsumer {

    private static final Logger log = LoggerFactory.getLogger(PedidoAuditConsumer.class);

    private final AuditProcessingService auditProcessingService;

    public PedidoAuditConsumer(AuditProcessingService auditProcessingService) {
        this.auditProcessingService = auditProcessingService;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.pedidos-audit:pedidos.auditoria.log}",
            groupId = "${app.kafka.groups.audit:pedidos360-audit-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onAuditLog(
            @Payload PedidoAuditoriaKafkaDto auditDto,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key
    ) {
        log.info("🛡️ [AUDIT CONSUMER] Registro recibido desde topic 'pedidos.auditoria.log' [Partición: {}, Offset: {}, Key: '{}']",
                partition, offset, key);

        // Delegar la lógica de dominio al servicio de auditoría
        auditProcessingService.procesarAuditoria(auditDto);
    }
}
