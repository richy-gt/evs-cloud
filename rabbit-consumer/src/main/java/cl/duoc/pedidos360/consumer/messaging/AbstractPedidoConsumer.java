package cl.duoc.pedidos360.consumer.messaging;

import cl.duoc.pedidos360.messaging.PedidoCreatedEvent;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.IOException;

@Slf4j
public abstract class AbstractPedidoConsumer {

    private static final String RETRY_HEADER = "x-retry-count";

    private final RabbitTemplate rabbitTemplate;
    private final String retryExchange;
    private final String routingKey;
    private final int maxRetries;

    protected AbstractPedidoConsumer(
            RabbitTemplate rabbitTemplate,
            String retryExchange,
            String routingKey,
            int maxRetries
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.retryExchange = retryExchange;
        this.routingKey = routingKey;
        this.maxRetries = maxRetries;
    }

    public final void consume(PedidoCreatedEvent event, Channel channel, Message message) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            process(event);
            channel.basicAck(deliveryTag, false);
        } catch (RecoverableProcessingException exception) {
            retryOrDeadLetter(channel, message, deliveryTag, event.numeroPedido(), exception);
        } catch (NonRecoverableProcessingException exception) {
            log.error("Mensaje no recuperable para pedido {}: {}", event.id(), exception.getMessage());
            channel.basicNack(deliveryTag, false, false);
        } catch (Exception exception) {
            log.error("Error inesperado procesando pedido {}; se enviara a DLQ", event.id(), exception);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    protected abstract void process(PedidoCreatedEvent event);

    private void retryOrDeadLetter(
            Channel channel,
            Message message,
            long deliveryTag,
            String orderNumber,
            RecoverableProcessingException exception
    ) throws IOException {
        Object currentCount = message.getMessageProperties().getHeaders().get(RETRY_HEADER);
        int retryCount = currentCount instanceof Number number ? number.intValue() : 0;
        if (retryCount >= maxRetries) {
            log.error("Se agotaron los reintentos para pedido {}; se enviara a DLQ", orderNumber, exception);
            channel.basicNack(deliveryTag, false, false);
            return;
        }

        message.getMessageProperties().setHeader(RETRY_HEADER, retryCount + 1);
        rabbitTemplate.send(retryExchange, routingKey, message);
        log.warn("Pedido {} reenviado a retry ({}/{})", orderNumber, retryCount + 1, maxRetries);
        channel.basicAck(deliveryTag, false);
    }
}