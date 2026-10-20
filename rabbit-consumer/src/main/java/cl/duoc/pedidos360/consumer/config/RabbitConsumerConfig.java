package cl.duoc.pedidos360.consumer.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
public class RabbitConsumerConfig {

    @Bean
    MessageConverter rabbitMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter rabbitMessageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(rabbitMessageConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    @Bean
    Declarables rabbitTopology(
            @Value("${app.rabbit.exchange}") String exchangeName,
            @Value("${app.rabbit.retry-exchange}") String retryExchangeName,
            @Value("${app.rabbit.dead-letter-exchange}") String deadLetterExchangeName,
            @Value("${app.rabbit.retry-delay}") int retryDelay,
            @Value("${app.rabbit.queues.notifications}") String notificationsQueueName,
            @Value("${app.rabbit.queues.tickets}") String ticketsQueueName,
            @Value("${app.rabbit.queues.documents}") String documentsQueueName,
            @Value("${app.rabbit.retry-queues.notifications}") String notificationsRetryName,
            @Value("${app.rabbit.retry-queues.tickets}") String ticketsRetryName,
            @Value("${app.rabbit.retry-queues.documents}") String documentsRetryName,
            @Value("${app.rabbit.dead-letter-queues.notifications}") String notificationsDlqName,
            @Value("${app.rabbit.dead-letter-queues.tickets}") String ticketsDlqName,
            @Value("${app.rabbit.dead-letter-queues.documents}") String documentsDlqName,
            @Value("${app.rabbit.routing-keys.notifications}") String notificationsRoutingKey,
            @Value("${app.rabbit.routing-keys.tickets}") String ticketsRoutingKey,
            @Value("${app.rabbit.routing-keys.documents}") String documentsRoutingKey
    ) {
        TopicExchange exchange = new TopicExchange(exchangeName, true, false);
        DirectExchange retryExchange = new DirectExchange(retryExchangeName, true, false);
        DirectExchange deadLetterExchange = new DirectExchange(deadLetterExchangeName, true, false);
        List<Declarable> declarations = new ArrayList<>(List.of(exchange, retryExchange, deadLetterExchange));

        declareDomain(declarations, exchange, retryExchange, deadLetterExchange, retryDelay,
                notificationsQueueName, notificationsRetryName, notificationsDlqName, notificationsRoutingKey);
        declareDomain(declarations, exchange, retryExchange, deadLetterExchange, retryDelay,
                ticketsQueueName, ticketsRetryName, ticketsDlqName, ticketsRoutingKey);
        declareDomain(declarations, exchange, retryExchange, deadLetterExchange, retryDelay,
                documentsQueueName, documentsRetryName, documentsDlqName, documentsRoutingKey);
        return new Declarables(declarations);
    }

    private void declareDomain(
            List<Declarable> declarations,
            TopicExchange exchange,
            DirectExchange retryExchange,
            DirectExchange deadLetterExchange,
            int retryDelay,
            String queueName,
            String retryQueueName,
            String dlqName,
            String routingKey
    ) {
        Queue queue = new Queue(queueName, true, false, false, Map.of(
                "x-dead-letter-exchange", deadLetterExchange.getName(),
                "x-dead-letter-routing-key", dlqName
        ));
        Queue retryQueue = new Queue(retryQueueName, true, false, false, Map.of(
                "x-message-ttl", retryDelay,
                "x-dead-letter-exchange", exchange.getName(),
                "x-dead-letter-routing-key", routingKey
        ));
        Queue deadLetterQueue = new Queue(dlqName, true);
        Binding eventBinding = BindingBuilder.bind(queue).to(exchange).with(routingKey);
        Binding retryBinding = BindingBuilder.bind(retryQueue).to(retryExchange).with(routingKey);
        Binding deadLetterBinding = BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(dlqName);
        declarations.addAll(List.of(queue, retryQueue, deadLetterQueue, eventBinding, retryBinding, deadLetterBinding));
    }
}