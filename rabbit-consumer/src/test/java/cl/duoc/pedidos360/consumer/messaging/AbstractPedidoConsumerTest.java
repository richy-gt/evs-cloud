package cl.duoc.pedidos360.consumer.messaging;

import cl.duoc.pedidos360.messaging.PedidoCreatedEvent;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AbstractPedidoConsumerTest {

    @Test
    void acknowledgesSuccessfulMessage() throws Exception {
        Channel channel = mock(Channel.class);
        TestConsumer consumer = new TestConsumer(mock(RabbitTemplate.class), null);

        consumer.consume(event(), channel, message());

        verify(channel).basicAck(9L, false);
    }

    @Test
    void retriesRecoverableFailureAndAcknowledgesOriginal() throws Exception {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        Channel channel = mock(Channel.class);
        TestConsumer consumer = new TestConsumer(rabbitTemplate, new RecoverableProcessingException("temporal", null));

        consumer.consume(event(), channel, message());

        verify(rabbitTemplate).send(eq("pedidos.retry"), eq("pedido.created.notifications"), any(Message.class));
        verify(channel).basicAck(9L, false);
    }

    @Test
    void rejectsNonRecoverableFailureForDeadLetterQueue() throws Exception {
        Channel channel = mock(Channel.class);
        TestConsumer consumer = new TestConsumer(mock(RabbitTemplate.class),
                new NonRecoverableProcessingException("invalid"));

        consumer.consume(event(), channel, message());

        verify(channel).basicNack(9L, false, false);
    }

    private PedidoCreatedEvent event() {
        return new PedidoCreatedEvent("ped-001", "PED-2026-001", "Cliente", "cliente@example.com", 100.0, "2026-01-01T00:00:00Z");
    }

    private Message message() {
        MessageProperties properties = new MessageProperties();
        properties.setDeliveryTag(9L);
        return new Message(new byte[]{1}, properties);
    }

    private static class TestConsumer extends AbstractPedidoConsumer {
        private final RuntimeException failure;

        TestConsumer(RabbitTemplate rabbitTemplate, RuntimeException failure) {
            super(rabbitTemplate, "pedidos.retry", "pedido.created.notifications", 3);
            this.failure = failure;
        }

        @Override
        protected void process(PedidoCreatedEvent event) {
            if (failure != null) {
                throw failure;
            }
        }
    }
}