package com.msa.order.global;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

    public static final String ORDER_QUEUE = "order.created";
    public static final String ORDER_CREATED_PAYMENT_QUEUE = "order.created.payment";
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_ROUTING_KEY = "order.created";

    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String PAYMENT_COMPLETED_QUEUE = "payment.completed";
    public static final String PAYMENT_FAILED_QUEUE = "payment.failed";

    public static final String STOCK_RESTORE_QUEUE = "stock.restore";

    public static final String CART_DELETE_QUEUE = "cart.delete";


    @Bean
    public Queue orderQueue() {
        return new Queue(ORDER_QUEUE, true);
    }

    @Bean
    public Queue orderCreatedPaymentQueue() {
        return new Queue(ORDER_CREATED_PAYMENT_QUEUE);
    }

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE);
    }

    @Bean
    public Binding binding(Queue orderQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(orderQueue).to(orderExchange).with(ORDER_ROUTING_KEY);
    }

    @Bean
    public Binding orderCreatedPaymentBinding(Queue orderCreatedPaymentQueue, DirectExchange orderExchange) {
          return BindingBuilder
                  .bind(orderCreatedPaymentQueue).to(orderExchange).with("order.created.payment");
    }

    @Bean
    public Queue paymentCompletedQueue() {
        return new Queue(PAYMENT_COMPLETED_QUEUE, true);
    }

    @Bean
    public Queue paymentFailedQueue() {
        return new Queue(PAYMENT_FAILED_QUEUE, true);
    }

    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(PAYMENT_EXCHANGE);
    }

    @Bean
    public Binding paymentCompletedBinding(Queue paymentCompletedQueue, DirectExchange paymentExchange) {
        return BindingBuilder.bind(paymentCompletedQueue).to(paymentExchange).with("payment.completed");
    }

    @Bean
    public Binding paymenFailedBinding(Queue paymentFailedQueue, DirectExchange paymentExchange) {
        return BindingBuilder.bind(paymentFailedQueue).to(paymentExchange).with("payment.failed");
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
    public Queue cartDeleteQueue() {
        return new Queue(CART_DELETE_QUEUE);
    }

    @Bean
    public Binding cartDeleteBinding(Queue cartDeleteQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(cartDeleteQueue).to(orderExchange).with("cart.delete");
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
