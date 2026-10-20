package cl.duoc.pedidos360.admin.service;

import cl.duoc.pedidos360.admin.dto.BindingRequest;
import cl.duoc.pedidos360.admin.dto.CreateExchangeRequest;
import cl.duoc.pedidos360.admin.dto.CreateQueueRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RabbitAdminService {

    private final RabbitAdmin rabbitAdmin;

    public RabbitAdminService(RabbitAdmin rabbitAdmin) {
        this.rabbitAdmin = rabbitAdmin;
    }

    public void createQueue(CreateQueueRequest request) {
        Queue queue = new Queue(request.name(), request.durable(), request.exclusive(), request.autoDelete(), request.arguments());
        rabbitAdmin.declareQueue(queue);
        log.info("Cola RabbitMQ declarada: {}", request.name());
    }

    public void deleteQueue(String name) {
        rabbitAdmin.deleteQueue(name);
        log.info("Cola RabbitMQ eliminada: {}", name);
    }

    public void createExchange(CreateExchangeRequest request) {
        ExchangeBuilder builder = switch (request.type().toLowerCase()) {
            case "direct" -> ExchangeBuilder.directExchange(request.name());
            case "topic" -> ExchangeBuilder.topicExchange(request.name());
            case "fanout" -> ExchangeBuilder.fanoutExchange(request.name());
            case "headers" -> ExchangeBuilder.headersExchange(request.name());
            default -> throw new IllegalArgumentException("Tipo de exchange no soportado: " + request.type());
        };
        if (request.internal()) {
            builder.internal();
        }
        if (request.autoDelete()) {
            builder.autoDelete();
        }
        Exchange exchange = builder
                .durable(request.durable())
                .withArguments(request.arguments())
                .build();
        rabbitAdmin.declareExchange(exchange);
        log.info("Exchange RabbitMQ declarado: {} ({})", request.name(), request.type());
    }

    public void deleteExchange(String name) {
        rabbitAdmin.deleteExchange(name);
        log.info("Exchange RabbitMQ eliminado: {}", name);
    }

    public void createBinding(BindingRequest request) {
        rabbitAdmin.declareBinding(toBinding(request));
        log.info("Binding RabbitMQ declarado para destino {}", request.destination());
    }

    public void deleteBinding(BindingRequest request) {
        rabbitAdmin.removeBinding(toBinding(request));
        log.info("Binding RabbitMQ eliminado para destino {}", request.destination());
    }

    private Binding toBinding(BindingRequest request) {
        Binding.DestinationType destinationType = Binding.DestinationType.valueOf(request.destinationType().toUpperCase());
        return new Binding(request.destination(), destinationType, request.exchange(), request.routingKey(), request.arguments());
    }
}