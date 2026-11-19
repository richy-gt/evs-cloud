package cl.duoc.pedidos360.consumer.listener;

import cl.duoc.pedidos360.consumer.service.AuditProcessingService;
import cl.duoc.pedidos360.kafka.dto.PedidoAuditoriaKafkaDto;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class PedidoAuditConsumerTest {

    @Test
    void onAuditLog_InvocaServicioDeAuditoria() {
        AuditProcessingService auditService = mock(AuditProcessingService.class);
        PedidoAuditConsumer consumer = new PedidoAuditConsumer(auditService);

        PedidoAuditoriaKafkaDto dto = new PedidoAuditoriaKafkaDto(
                "evt-10", "ped-10", "PED-2025-010", "CAMBIO_ESTADO",
                "PENDIENTE", "DESPACHADO", "ADMIN", "WEB", 10000.0, "2025-03-10T14:30:00Z"
        );

        consumer.onAuditLog(dto, 1, 5L, "WEB");

        verify(auditService, times(1)).procesarAuditoria(dto);
    }
}
