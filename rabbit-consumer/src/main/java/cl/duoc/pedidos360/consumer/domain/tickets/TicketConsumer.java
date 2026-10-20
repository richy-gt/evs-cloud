package cl.duoc.pedidos360.consumer.domain.tickets;

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
public class TicketConsumer extends AbstractPedidoConsumer {

    public TicketConsumer(
            RabbitTemplate rabbitTemplate,
            @Value("${app.rabbit.retry-exchange}") String retryExchange,
            @Value("${app.rabbit.routing-keys.tickets}") String routingKey,
            @Value("${app.rabbit.max-retries}") int maxRetries
    ) {
        super(rabbitTemplate, retryExchange, routingKey, maxRetries);
    }

    @RabbitListener(queues = "${app.rabbit.queues.tickets}", containerFactory = "rabbitListenerContainerFactory")
    public void onPedidoCreated(PedidoCreatedEvent event, Channel channel, Message message) throws Exception {
        consume(event, channel, message);
    }

    @Override
    protected void process(PedidoCreatedEvent event) {
        if (event.id() == null || event.id().isBlank()) {
            throw new NonRecoverableProcessingException("El evento no contiene ID de pedido");
        }
        log.info("Ticket preparado para pedido {}", event.numeroPedido());
    }
}