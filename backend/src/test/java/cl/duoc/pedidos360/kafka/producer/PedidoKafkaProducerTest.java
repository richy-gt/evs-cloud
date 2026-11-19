package cl.duoc.pedidos360.kafka.producer;

import cl.duoc.pedidos360.entity.ItemPedidoEntity;
import cl.duoc.pedidos360.entity.PedidoEntity;
import cl.duoc.pedidos360.kafka.dto.PedidoAuditoriaKafkaDto;
import cl.duoc.pedidos360.kafka.dto.PedidoEventKafkaDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PedidoKafkaProducerTest {

    private KafkaTemplate<String, Object> kafkaTemplate;
    private PedidoKafkaProducer producer;

    private static final String STREAM_TOPIC = "pedidos.streaming.eventos";
    private static final String AUDIT_TOPIC = "pedidos.auditoria.log";

    @BeforeEach
    void setUp() {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, Object> mockTemplate = mock(KafkaTemplate.class);
        this.kafkaTemplate = mockTemplate;
        this.producer = new PedidoKafkaProducer(kafkaTemplate, STREAM_TOPIC, AUDIT_TOPIC);
    }

    @Test
    void publicarEventoPedido_DebeEnviarMensajeConKeyNumeroPedido() {
        PedidoEntity pedido = new PedidoEntity();
        pedido.setId("ped-100");
        pedido.setNumeroPedido("PED-2025-100");
        pedido.setClienteNombre("Cliente Prueba");
        pedido.setClienteEmail("cliente@prueba.cl");
        pedido.setTotal(50000.0);
        pedido.addItem(new ItemPedidoEntity("it-1", "Producto A", "SKU-A", 2, 25000.0, 50000.0));

        when(kafkaTemplate.send(eq(STREAM_TOPIC), eq("PED-2025-100"), any()))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        producer.publicarEventoPedido(pedido, "PEDIDO_CREADO");

        ArgumentCaptor<PedidoEventKafkaDto> captor = ArgumentCaptor.forClass(PedidoEventKafkaDto.class);
        verify(kafkaTemplate, times(1)).send(eq(STREAM_TOPIC), eq("PED-2025-100"), captor.capture());

        PedidoEventKafkaDto dto = captor.getValue();
        assertEquals("ped-100", dto.id());
        assertEquals("PED-2025-100", dto.numeroPedido());
        assertEquals("PEDIDO_CREADO", dto.tipoEvento());
        assertEquals(50000.0, dto.total());
        assertEquals(1, dto.items().size());
        assertEquals("Producto A", dto.items().get(0).producto());
        assertEquals("SKU-A", dto.items().get(0).sku());
    }

    @Test
    void publicarAuditoria_DebeEnviarMensajeAlTopicoDeAuditoria() {
        PedidoEntity pedido = new PedidoEntity();
        pedido.setId("ped-101");
        pedido.setNumeroPedido("PED-2025-101");
        pedido.setCanalVenta("WEB");
        pedido.setTotal(120000.0);

        when(kafkaTemplate.send(eq(AUDIT_TOPIC), eq("WEB"), any()))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        producer.publicarAuditoria(pedido, "CAMBIO_ESTADO", "PENDIENTE", "DESPACHADO", "ADMIN_USER");

        ArgumentCaptor<PedidoAuditoriaKafkaDto> captor = ArgumentCaptor.forClass(PedidoAuditoriaKafkaDto.class);
        verify(kafkaTemplate, times(1)).send(eq(AUDIT_TOPIC), eq("WEB"), captor.capture());

        PedidoAuditoriaKafkaDto dto = captor.getValue();
        assertEquals("ped-101", dto.pedidoId());
        assertEquals("PED-2025-101", dto.numeroPedido());
        assertEquals("CAMBIO_ESTADO", dto.accion());
        assertEquals("PENDIENTE", dto.estadoAnterior());
        assertEquals("DESPACHADO", dto.estadoNuevo());
        assertEquals("ADMIN_USER", dto.usuario());
        assertEquals("WEB", dto.canalVenta());
        assertEquals(120000.0, dto.monto());
        assertNotNull(dto.eventoId());
    }
}
