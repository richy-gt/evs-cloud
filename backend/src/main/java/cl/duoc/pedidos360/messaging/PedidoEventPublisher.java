package cl.duoc.pedidos360.messaging;

import cl.duoc.pedidos360.entity.PedidoEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PedidoEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String notificationsRoutingKey;
    private final String ticketsRoutingKey;
    private final String documentsRoutingKey;

    public PedidoEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${app.rabbit.exchange}") String exchange,
            @Value("${app.rabbit.routing-keys.notifications}") String notificationsRoutingKey,
            @Value("${app.rabbit.routing-keys.tickets}") String ticketsRoutingKey,
            @Value("${app.rabbit.routing-keys.documents}") String documentsRoutingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.notificationsRoutingKey = notificationsRoutingKey;
        this.ticketsRoutingKey = ticketsRoutingKey;
        this.documentsRoutingKey = documentsRoutingKey;
    }

    public void publishPedidoCreado(PedidoEntity pedido) {
        PedidoCreatedEvent event = new PedidoCreatedEvent(
                pedido.getId(),
                pedido.getNumeroPedido(),
                pedido.getClienteNombre(),
                pedido.getClienteEmail(),
                pedido.getTotal(),
                pedido.getFechaCreacion()
        );
        rabbitTemplate.convertAndSend(exchange, notificationsRoutingKey, event);
        rabbitTemplate.convertAndSend(exchange, ticketsRoutingKey, event);
        rabbitTemplate.convertAndSend(exchange, documentsRoutingKey, event);
    }
}