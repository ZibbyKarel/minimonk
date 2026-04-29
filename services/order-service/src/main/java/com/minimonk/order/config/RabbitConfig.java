package com.minimonk.order.config;

import com.minimonk.events.RabbitEventPublisher;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
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
    public static final String STOCK_RESERVED_QUEUE = "orders.stock-reserved";
    public static final String STOCK_FAILED_QUEUE = "orders.stock-failed";
    public static final String PAYMENT_SUCCEEDED_QUEUE = "orders.payment-succeeded";
    public static final String PAYMENT_FAILED_QUEUE = "orders.payment-failed";
    public static final String STOCK_RELEASED_QUEUE = "orders.stock-released";

    @Bean
    TopicExchange eventExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    Queue stockReservedQueue() {
        return new Queue(STOCK_RESERVED_QUEUE, true);
    }

    @Bean
    Queue stockFailedQueue() {
        return new Queue(STOCK_FAILED_QUEUE, true);
    }

    @Bean
    Queue paymentSucceededQueue() {
        return new Queue(PAYMENT_SUCCEEDED_QUEUE, true);
    }

    @Bean
    Queue paymentFailedQueue() {
        return new Queue(PAYMENT_FAILED_QUEUE, true);
    }

    @Bean
    Queue stockReleasedQueue() {
        return new Queue(STOCK_RELEASED_QUEUE, true);
    }

    @Bean
    Binding stockReservedBinding(TopicExchange eventExchange, Queue stockReservedQueue) {
        return BindingBuilder.bind(stockReservedQueue).to(eventExchange).with("stock.reserved");
    }

    @Bean
    Binding stockFailedBinding(TopicExchange eventExchange, Queue stockFailedQueue) {
        return BindingBuilder.bind(stockFailedQueue).to(eventExchange).with("stock.reservation.failed");
    }

    @Bean
    Binding paymentSucceededBinding(TopicExchange eventExchange, Queue paymentSucceededQueue) {
        return BindingBuilder.bind(paymentSucceededQueue).to(eventExchange).with("payment.succeeded");
    }

    @Bean
    Binding paymentFailedBinding(TopicExchange eventExchange, Queue paymentFailedQueue) {
        return BindingBuilder.bind(paymentFailedQueue).to(eventExchange).with("payment.failed");
    }

    @Bean
    Binding stockReleasedBinding(TopicExchange eventExchange, Queue stockReleasedQueue) {
        return BindingBuilder.bind(stockReleasedQueue).to(eventExchange).with("stock.released");
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
}
