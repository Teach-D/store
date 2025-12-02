package com.msa.product.global.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

    public static final String ORDER_EXCHANGE = "order.exchange";

    public static final String STOCK_RESTORE_QUEUE = "stock.restore";

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE);
    }

    @Bean
    public Queue stockRestoreQueue() {
        return new Queue(STOCK_RESTORE_QUEUE, true);
    }

    @Bean
    public Binding stockRestoreBinding(Queue stockRestoreQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(stockRestoreQueue).to(orderExchange).with("stock.restore");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new
                Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}
