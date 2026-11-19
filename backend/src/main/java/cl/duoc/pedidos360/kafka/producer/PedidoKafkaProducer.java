package cl.duoc.pedidos360.kafka.producer;

import cl.duoc.pedidos360.entity.PedidoEntity;
import cl.duoc.pedidos360.kafka.dto.PedidoAuditoriaKafkaDto;
import cl.duoc.pedidos360.kafka.dto.PedidoEventKafkaDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PedidoKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(PedidoKafkaProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String streamTopic;
    private final String auditTopic;

    public PedidoKafkaProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.topics.pedidos-stream:pedidos.streaming.eventos}") String streamTopic,
            @Value("${app.kafka.topics.pedidos-audit:pedidos.auditoria.log}") String auditTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.streamTopic = streamTopic;
        this.auditTopic = auditTopic;
    }

    public void publicarEventoPedido(PedidoEntity pedido, String tipoEvento) {
        List<PedidoEventKafkaDto.ItemKafkaDto> itemsDto = (pedido.getItems() != null)
                ? pedido.getItems().stream()
                        .map(item -> new PedidoEventKafkaDto.ItemKafkaDto(
                                item.getId(),
                                item.getProducto(),
                                item.getSku(),
                                item.getCantidad(),
                                item.getPrecioUnitario(),
                                item.getSubtotal()
                        ))
                        .collect(Collectors.toList())
                : Collections.emptyList();

        PedidoEventKafkaDto event = new PedidoEventKafkaDto(
                pedido.getId(),
                pedido.getNumeroPedido(),
                pedido.getClienteNombre(),
                pedido.getClienteEmail(),
                pedido.getClienteTelefono(),
                pedido.getDireccionEnvio(),
                pedido.getCiudad(),
                pedido.getFechaCreacion(),
                pedido.getFechaEntregaEstimada(),
                pedido.getEstado(),
                pedido.getPrioridad(),
                pedido.getCanalVenta(),
                pedido.getTotal(),
                tipoEvento,
                Instant.now().toString(),
                itemsDto
        );

        // Envío con clave 'numeroPedido' para garantizar orden secuencial en la misma particion
        var future = kafkaTemplate.send(streamTopic, pedido.getNumeroPedido(), event);
        if (future != null) {
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    int partition = (result != null && result.getRecordMetadata() != null) ? result.getRecordMetadata().partition() : 0;
                    long offset = (result != null && result.getRecordMetadata() != null) ? result.getRecordMetadata().offset() : 0;
                    log.info("Evento Kafka publicado a '{}' [Key: {}, Partition: {}, Offset: {}]",
                            streamTopic, pedido.getNumeroPedido(), partition, offset);
                } else {
                    log.error("Error al publicar evento Kafka a '{}' [Key: {}]: {}",
                            streamTopic, pedido.getNumeroPedido(), ex.getMessage());
                }
            });
        }
    }

    public void publicarAuditoria(PedidoEntity pedido, String accion, String estadoAnterior, String estadoNuevo, String usuario) {
        PedidoAuditoriaKafkaDto auditEvent = new PedidoAuditoriaKafkaDto(
                UUID.randomUUID().toString(),
                pedido.getId(),
                pedido.getNumeroPedido(),
                accion,
                estadoAnterior,
                estadoNuevo,
                usuario != null ? usuario : "SISTEMA",
                pedido.getCanalVenta() != null ? pedido.getCanalVenta() : "PORTAL",
                pedido.getTotal(),
                Instant.now().toString()
        );

        String key = (pedido.getCanalVenta() != null && !pedido.getCanalVenta().isBlank())
                ? pedido.getCanalVenta()
                : pedido.getId();

        var future = kafkaTemplate.send(auditTopic, key, auditEvent);
        if (future != null) {
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    int partition = (result != null && result.getRecordMetadata() != null) ? result.getRecordMetadata().partition() : 0;
                    long offset = (result != null && result.getRecordMetadata() != null) ? result.getRecordMetadata().offset() : 0;
                    log.info("Auditoria Kafka publicada a '{}' [Key: {}, Partition: {}, Offset: {}]",
                            auditTopic, key, partition, offset);
                } else {
                    log.error("Error al publicar auditoria Kafka a '{}' [Key: {}]: {}",
                            auditTopic, key, ex.getMessage());
                }
            });
        }
    }

    public String getStreamTopic() {
        return streamTopic;
    }

    public String getAuditTopic() {
        return auditTopic;
    }
}
