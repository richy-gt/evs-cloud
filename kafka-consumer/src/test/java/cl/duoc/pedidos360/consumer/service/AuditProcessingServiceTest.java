package cl.duoc.pedidos360.consumer.service;

import cl.duoc.pedidos360.kafka.dto.PedidoAuditoriaKafkaDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuditProcessingServiceTest {

    private AuditProcessingService service;

    @BeforeEach
    void setUp() {
        service = new AuditProcessingService();
    }

    @Test
    void procesarAuditoria_Valida_RegistraCorrectamente() {
        PedidoAuditoriaKafkaDto dto = new PedidoAuditoriaKafkaDto(
                "evt-001",
                "ped-001",
                "PED-2025-001",
                "CREACION_PEDIDO",
                null,
                "PENDIENTE",
                "JUAN_PEREZ",
                "WEB",
                80000.0,
                "2025-03-10T14:30:00Z"
        );

        service.procesarAuditoria(dto);

        assertEquals(1, service.getTotalAuditorias());
    }

    @Test
    void procesarAuditoria_SinPedidoId_LanzaExcepcion() {
        PedidoAuditoriaKafkaDto dto = new PedidoAuditoriaKafkaDto(
                "evt-002",
                null,
                "PED-2025-002",
                "CREACION_PEDIDO",
                null,
                "PENDIENTE",
                "JUAN",
                "WEB",
                100.0,
                "2025-03-10T14:30:00Z"
        );

        assertThrows(IllegalArgumentException.class, () -> service.procesarAuditoria(dto));
    }
}
