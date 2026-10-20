package cl.duoc.pedidos360.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RabbitTopologyConfig {

    @Bean
    MessageConverter rabbitMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    TopicExchange pedidosExchange(@Value("${app.rabbit.exchange}") String name) {
        return new TopicExchange(name, true, false);
    }

    @Bean
    DirectExchange retryExchange(@Value("${app.rabbit.retry-exchange}") String name) {
        return new DirectExchange(name, true, false);
    }

    @Bean
    DirectExchange deadLetterExchange(@Value("${app.rabbit.dead-letter-exchange}") String name) {
        return new DirectExchange(name, true, false);
    }

    @Bean
    Queue notificationsQueue(
            @Value("${app.rabbit.queues.notifications}") String name,
            @Value("${app.rabbit.dead-letter-exchange}") String deadLetterExchange,
            @Value("${app.rabbit.dead-letter-queues.notifications}") String deadLetterRoutingKey
    ) {
        return deadLetteringQueue(name, deadLetterExchange, deadLetterRoutingKey);
    }

    @Bean
    Queue ticketsQueue(
            @Value("${app.rabbit.queues.tickets}") String name,
            @Value("${app.rabbit.dead-letter-exchange}") String deadLetterExchange,
            @Value("${app.rabbit.dead-letter-queues.tickets}") String deadLetterRoutingKey
    ) {
        return deadLetteringQueue(name, deadLetterExchange, deadLetterRoutingKey);
    }

    @Bean
    Queue documentsQueue(
            @Value("${app.rabbit.queues.documents}") String name,
            @Value("${app.rabbit.dead-letter-exchange}") String deadLetterExchange,
            @Value("${app.rabbit.dead-letter-queues.documents}") String deadLetterRoutingKey
    ) {
        return deadLetteringQueue(name, deadLetterExchange, deadLetterRoutingKey);
    }

    @Bean
    Queue notificationsRetryQueue(
            @Value("${app.rabbit.retry-queues.notifications}") String name,
            @Value("${app.rabbit.retry-delay}") int retryDelay,
            @Value("${app.rabbit.exchange}") String exchange,
            @Value("${app.rabbit.routing-keys.notifications}") String routingKey
    ) {
        return retryQueue(name, retryDelay, exchange, routingKey);
    }

    @Bean
    Queue ticketsRetryQueue(
            @Value("${app.rabbit.retry-queues.tickets}") String name,
            @Value("${app.rabbit.retry-delay}") int retryDelay,
            @Value("${app.rabbit.exchange}") String exchange,
            @Value("${app.rabbit.routing-keys.tickets}") String routingKey
    ) {
        return retryQueue(name, retryDelay, exchange, routingKey);
    }

    @Bean
    Queue documentsRetryQueue(
            @Value("${app.rabbit.retry-queues.documents}") String name,
            @Value("${app.rabbit.retry-delay}") int retryDelay,
            @Value("${app.rabbit.exchange}") String exchange,
            @Value("${app.rabbit.routing-keys.documents}") String routingKey
    ) {
        return retryQueue(name, retryDelay, exchange, routingKey);
    }

    @Bean
    Queue notificationsDlq(@Value("${app.rabbit.dead-letter-queues.notifications}") String name) {
        return new Queue(name, true);
    }

    @Bean
    Queue ticketsDlq(@Value("${app.rabbit.dead-letter-queues.tickets}") String name) {
        return new Queue(name, true);
    }

    @Bean
    Queue documentsDlq(@Value("${app.rabbit.dead-letter-queues.documents}") String name) {
        return new Queue(name, true);
    }

    @Bean
    Binding notificationsBinding(
            @Qualifier("notificationsQueue") Queue notificationsQueue,
            @Qualifier("pedidosExchange") TopicExchange pedidosExchange,
            @Value("${app.rabbit.routing-keys.notifications}") String routingKey
    ) {
        return BindingBuilder.bind(notificationsQueue).to(pedidosExchange).with(routingKey);
    }

    @Bean
    Binding ticketsBinding(
            @Qualifier("ticketsQueue") Queue ticketsQueue,
            @Qualifier("pedidosExchange") TopicExchange pedidosExchange,
            @Value("${app.rabbit.routing-keys.tickets}") String routingKey
    ) {
        return BindingBuilder.bind(ticketsQueue).to(pedidosExchange).with(routingKey);
    }

    @Bean
    Binding documentsBinding(
            @Qualifier("documentsQueue") Queue documentsQueue,
            @Qualifier("pedidosExchange") TopicExchange pedidosExchange,
            @Value("${app.rabbit.routing-keys.documents}") String routingKey
    ) {
        return BindingBuilder.bind(documentsQueue).to(pedidosExchange).with(routingKey);
    }

    @Bean
    Binding notificationsDlqBinding(
            @Qualifier("notificationsDlq") Queue notificationsDlq,
            @Qualifier("deadLetterExchange") DirectExchange deadLetterExchange,
            @Value("${app.rabbit.dead-letter-queues.notifications}") String routingKey
    ) {
        return BindingBuilder.bind(notificationsDlq).to(deadLetterExchange).with(routingKey);
    }

    @Bean
    Binding ticketsDlqBinding(
            @Qualifier("ticketsDlq") Queue ticketsDlq,
            @Qualifier("deadLetterExchange") DirectExchange deadLetterExchange,
            @Value("${app.rabbit.dead-letter-queues.tickets}") String routingKey
    ) {
        return BindingBuilder.bind(ticketsDlq).to(deadLetterExchange).with(routingKey);
    }

    @Bean
    Binding documentsDlqBinding(
            @Qualifier("documentsDlq") Queue documentsDlq,
            @Qualifier("deadLetterExchange") DirectExchange deadLetterExchange,
            @Value("${app.rabbit.dead-letter-queues.documents}") String routingKey
    ) {
        return BindingBuilder.bind(documentsDlq).to(deadLetterExchange).with(routingKey);
    }

    @Bean
    Binding notificationsRetryBinding(
            @Qualifier("notificationsRetryQueue") Queue notificationsRetryQueue,
            @Qualifier("retryExchange") DirectExchange retryExchange,
            @Value("${app.rabbit.routing-keys.notifications}") String routingKey
    ) {
        return BindingBuilder.bind(notificationsRetryQueue).to(retryExchange).with(routingKey);
    }

    @Bean
    Binding ticketsRetryBinding(
            @Qualifier("ticketsRetryQueue") Queue ticketsRetryQueue,
            @Qualifier("retryExchange") DirectExchange retryExchange,
            @Value("${app.rabbit.routing-keys.tickets}") String routingKey
    ) {
        return BindingBuilder.bind(ticketsRetryQueue).to(retryExchange).with(routingKey);
    }

    @Bean
    Binding documentsRetryBinding(
            @Qualifier("documentsRetryQueue") Queue documentsRetryQueue,
            @Qualifier("retryExchange") DirectExchange retryExchange,
            @Value("${app.rabbit.routing-keys.documents}") String routingKey
    ) {
        return BindingBuilder.bind(documentsRetryQueue).to(retryExchange).with(routingKey);
    }

    private Queue deadLetteringQueue(String name, String exchange, String routingKey) {
        return new Queue(name, true, false, false, Map.of(
                "x-dead-letter-exchange", exchange,
                "x-dead-letter-routing-key", routingKey
        ));
    }

    private Queue retryQueue(String name, int retryDelay, String exchange, String routingKey) {
        return new Queue(name, true, false, false, Map.of(
                "x-message-ttl", retryDelay,
                "x-dead-letter-exchange", exchange,
                "x-dead-letter-routing-key", routingKey
        ));
    }
}