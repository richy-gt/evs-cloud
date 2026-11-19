package cl.duoc.pedidos360.consumer.service;

import cl.duoc.pedidos360.kafka.dto.PedidoEventKafkaDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class AnalyticsProcessingServiceTest {

    private AnalyticsProcessingService service;

    @BeforeEach
    void setUp() {
        service = new AnalyticsProcessingService();
    }

    @Test
    void procesarEventoPedido_Valido_ActualizaMetricas() {
        PedidoEventKafkaDto event = new PedidoEventKafkaDto(
                "ped-001",
                "PED-2025-001",
                "Cliente Test",
                "test@correo.cl",
                "+56912345678",
                "Av Test 123",
                "Santiago",
                "2025-03-10T14:30:00Z",
                "2025-03-15T18:00:00Z",
                "PENDIENTE",
                "ALTA",
                "WEB",
                150000.0,
                "PEDIDO_CREADO",
                "2025-03-10T14:30:05Z",
                Collections.emptyList()
        );

        service.procesarEventoPedido(event);

        assertEquals(1, service.getTotalEventosProcesados());
        assertEquals(150000.0, service.getMontoTotalAcumulado());
    }

    @Test
    void procesarEventoPedido_SinNumeroPedido_LanzaExcepcion() {
        PedidoEventKafkaDto invalidEvent = new PedidoEventKafkaDto(
                "ped-002",
                null,
                "Cliente Test",
                null, null, null, null, null, null, "PENDIENTE", null, null, 100.0, "PEDIDO_CREADO", null, null
        );

        assertThrows(IllegalArgumentException.class, () -> service.procesarEventoPedido(invalidEvent));
    }

    @Test
    void procesarEventoPedido_MontoNegativo_LanzaExcepcion() {
        PedidoEventKafkaDto negativeTotalEvent = new PedidoEventKafkaDto(
                "ped-003",
                "PED-2025-003",
                "Cliente Test",
                null, null, null, null, null, null, "PENDIENTE", null, null, -500.0, "PEDIDO_CREADO", null, null
        );

        assertThrows(IllegalArgumentException.class, () -> service.procesarEventoPedido(negativeTotalEvent));
    }
}
