package cl.duoc.pedidos360.consumer.domain.notifications;

import cl.duoc.pedidos360.consumer.messaging.AbstractPedidoConsumer;
import cl.duoc.pedidos360.consumer.messaging.NonRecoverableProcessingException;
import cl.duoc.pedidos360.messaging.PedidoCreatedEvent;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationConsumer extends AbstractPedidoConsumer {

    public NotificationConsumer(
            RabbitTemplate rabbitTemplate,
            @Value("${app.rabbit.retry-exchange}") String retryExchange,
            @Value("${app.rabbit.routing-keys.notifications}") String routingKey,
            @Value("${app.rabbit.max-retries}") int maxRetries
    ) {
        super(rabbitTemplate, retryExchange, routingKey, maxRetries);
    }

    @RabbitListener(queues = "${app.rabbit.queues.notifications}", containerFactory = "rabbitListenerContainerFactory")
    public void onPedidoCreated(PedidoCreatedEvent event, Channel channel, Message message) throws Exception {
        consume(event, channel, message);
    }

    @Override
    protected void process(PedidoCreatedEvent event) {
        if (event.clienteEmail() == null || event.clienteEmail().isBlank()) {
            throw new NonRecoverableProcessingException("El pedido no tiene correo de cliente");
        }
        log.info("Notificacion preparada para {} por pedido {}", event.clienteEmail(), event.numeroPedido());
    }
}