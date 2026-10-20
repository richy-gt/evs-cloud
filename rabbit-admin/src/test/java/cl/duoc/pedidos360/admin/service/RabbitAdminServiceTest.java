package cl.duoc.pedidos360.admin.service;

import cl.duoc.pedidos360.admin.dto.BindingRequest;
import cl.duoc.pedidos360.admin.dto.CreateExchangeRequest;
import cl.duoc.pedidos360.admin.dto.CreateQueueRequest;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RabbitAdminServiceTest {

    @Test
    void declaresQueueThroughRabbitAdmin() {
        RabbitAdmin rabbitAdmin = mock(RabbitAdmin.class);
        RabbitAdminService service = new RabbitAdminService(rabbitAdmin);

        service.createQueue(new CreateQueueRequest("pedidos.test", true, false, false, Map.of()));

        verify(rabbitAdmin).declareQueue(any(Queue.class));
    }

    @Test
    void declaresTopicExchangeAndBindingThroughRabbitAdmin() {
        RabbitAdmin rabbitAdmin = mock(RabbitAdmin.class);
        RabbitAdminService service = new RabbitAdminService(rabbitAdmin);

        service.createExchange(new CreateExchangeRequest("pedidos.test", "topic", true, false, false, Map.of()));
        service.createBinding(new BindingRequest("pedidos.test", "QUEUE", "pedidos.events", "pedido.created", Map.of()));

        verify(rabbitAdmin).declareExchange(any(Exchange.class));
        verify(rabbitAdmin).declareBinding(any(Binding.class));
    }
}