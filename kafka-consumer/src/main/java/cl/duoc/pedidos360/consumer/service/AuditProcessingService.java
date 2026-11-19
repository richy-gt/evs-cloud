package cl.duoc.pedidos360.consumer.service;

import cl.duoc.pedidos360.kafka.dto.PedidoAuditoriaKafkaDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AuditProcessingService {

    private static final Logger log = LoggerFactory.getLogger(AuditProcessingService.class);

    private final AtomicInteger totalAuditorias = new AtomicInteger(0);
    private final ConcurrentLinkedQueue<PedidoAuditoriaKafkaDto> historialReciente = new ConcurrentLinkedQueue<>();

    public void procesarAuditoria(PedidoAuditoriaKafkaDto auditDto) {
        if (auditDto == null) {
            throw new IllegalArgumentException("El registro de auditoría no puede ser nulo");
        }
        if (auditDto.pedidoId() == null || auditDto.pedidoId().isBlank()) {
            throw new IllegalArgumentException("El ID de pedido es mandatorio para registrar auditoría");
        }

        totalAuditorias.incrementAndGet();
        historialReciente.add(auditDto);
        if (historialReciente.size() > 50) {
            historialReciente.poll();
        }

        log.info("🛡️ [AUDITORIA] Registrada acción '{}' sobre pedido '{}' ({}) por usuario '{}' [Canal: {}] - Estado: '{}' -> '{}' a las {}",
                auditDto.accion(), auditDto.pedidoId(), auditDto.numeroPedido(), auditDto.usuario(),
                auditDto.canalVenta(), auditDto.estadoAnterior(), auditDto.estadoNuevo(), auditDto.timestamp());
    }

    public int getTotalAuditorias() {
        return totalAuditorias.get();
    }
}
