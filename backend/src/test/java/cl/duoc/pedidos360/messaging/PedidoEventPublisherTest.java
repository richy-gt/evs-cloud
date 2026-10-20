package cl.duoc.pedidos360.messaging;

import cl.duoc.pedidos360.entity.PedidoEntity;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class PedidoEventPublisherTest {

    @Test
    void publishesCreatedOrderToAllDomainRoutes() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        PedidoEventPublisher publisher = new PedidoEventPublisher(
                rabbitTemplate, "pedidos.events", "pedido.created.notifications",
                "pedido.created.tickets", "pedido.created.documents");
        PedidoEntity order = new PedidoEntity();
        order.setId("ped-001");
        order.setNumeroPedido("PED-2026-001");

        publisher.publishPedidoCreado(order);

        verify(rabbitTemplate, times(1)).convertAndSend("pedidos.events", "pedido.created.notifications",
                new PedidoCreatedEvent("ped-001", "PED-2026-001", null, null, null, null));
        verify(rabbitTemplate, times(1)).convertAndSend("pedidos.events", "pedido.created.tickets",
                new PedidoCreatedEvent("ped-001", "PED-2026-001", null, null, null, null));
        verify(rabbitTemplate, times(1)).convertAndSend("pedidos.events", "pedido.created.documents",
                new PedidoCreatedEvent("ped-001", "PED-2026-001", null, null, null, null));
    }
}