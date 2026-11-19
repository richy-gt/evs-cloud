package cl.duoc.pedidos360.consumer.listener;

import cl.duoc.pedidos360.consumer.service.AnalyticsProcessingService;
import cl.duoc.pedidos360.kafka.dto.PedidoEventKafkaDto;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.mockito.Mockito.*;

class PedidoStreamConsumerTest {

    @Test
    void onPedidoEvent_InvocaServicioDeAnalitica() {
        AnalyticsProcessingService analyticsService = mock(AnalyticsProcessingService.class);
        PedidoStreamConsumer consumer = new PedidoStreamConsumer(analyticsService);

        PedidoEventKafkaDto event = new PedidoEventKafkaDto(
                "ped-001", "PED-2025-001", "Cliente A", "correo@a.cl", null,
                null, "Santiago", "2025-03-10T14:30:00Z", null, "PENDIENTE",
                "MEDIA", "WEB", 50000.0, "PEDIDO_CREADO", "2025-03-10T14:30:00Z", Collections.emptyList()
        );

        consumer.onPedidoEvent(event, 0, 10L, "PED-2025-001");

        verify(analyticsService, times(1)).procesarEventoPedido(event);
    }
}
