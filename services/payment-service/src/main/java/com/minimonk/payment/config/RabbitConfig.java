package com.minimonk.payment.config;

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
    public static final String STOCK_RESERVED_QUEUE = "payments.stock-reserved";

    @Bean
    TopicExchange eventExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    Queue stockReservedQueue() {
        return durableQueue(STOCK_RESERVED_QUEUE);
    }

    @Bean
    Queue paymentDeadLetterQueue() {
        return new Queue("payments.dead-letter", true);
    }

    @Bean
    Binding stockReservedBinding(TopicExchange eventExchange, Queue stockReservedQueue) {
        return BindingBuilder.bind(stockReservedQueue).to(eventExchange).with("stock.reserved");
    }

    @Bean
    Binding paymentDeadLetterBinding(TopicExchange eventExchange, Queue paymentDeadLetterQueue) {
        return BindingBuilder.bind(paymentDeadLetterQueue).to(eventExchange).with("payments.#.dead");
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
