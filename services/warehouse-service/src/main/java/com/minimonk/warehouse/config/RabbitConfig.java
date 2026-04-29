package com.minimonk.warehouse.config;

import com.minimonk.events.RabbitEventPublisher;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String EXCHANGE = "minimonk.events";
    public static final String ORDER_CREATED_QUEUE = "warehouse.order-created";
    public static final String PAYMENT_FAILED_QUEUE = "warehouse.payment-failed";

    @Bean
    TopicExchange eventExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    Queue orderCreatedQueue() {
        return durableQueue(ORDER_CREATED_QUEUE);
    }

    @Bean
    Queue paymentFailedQueue() {
        return durableQueue(PAYMENT_FAILED_QUEUE);
    }

    @Bean
    Queue warehouseDeadLetterQueue() {
        return new Queue("warehouse.dead-letter", true);
    }

    @Bean
    Binding orderCreatedBinding(TopicExchange eventExchange, Queue orderCreatedQueue) {
        return BindingBuilder.bind(orderCreatedQueue).to(eventExchange).with("order.created");
    }

    @Bean
    Binding paymentFailedBinding(TopicExchange eventExchange, Queue paymentFailedQueue) {
        return BindingBuilder.bind(paymentFailedQueue).to(eventExchange).with("payment.failed");
    }

    @Bean
    Binding warehouseDeadLetterBinding(TopicExchange eventExchange, Queue warehouseDeadLetterQueue) {
        return BindingBuilder.bind(warehouseDeadLetterQueue).to(eventExchange).with("warehouse.#.dead");
    }

    @Bean
    Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }

    @Bean
    RabbitEventPublisher rabbitEventPublisher(RabbitTemplate rabbitTemplate) {
        return new RabbitEventPublisher(rabbitTemplate, EXCHANGE);
    }

    @Bean
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        return factory;
    }

    private Queue durableQueue(String name) {
        return QueueBuilder.durable(name)
                .deadLetterExchange(EXCHANGE)
                .deadLetterRoutingKey(name + ".dead")
                .build();
    }
}
