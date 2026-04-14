package com.msa.member.global;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

    public static final String CART_DELETE_QUEUE = "cart.delete";
    public static final String ORDER_EXCHANGE = "order.exchange";

    // DLX / DLQ
    public static final String DLX_EXCHANGE = "dlx.exchange";
    public static final String CART_DELETE_DLQ = "cart.delete.dlq";


    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE);
    }

    // cart.delete — DLX 연결
    @Bean
    public Queue cartDeleteQueue() {
        return QueueBuilder.durable(CART_DELETE_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", CART_DELETE_QUEUE)
                .build();
    }

    @Bean
    public Binding cartDeleteBinding(Queue cartDeleteQueue, @Qualifier("orderExchange") DirectExchange orderExchange) {
        return BindingBuilder.bind(cartDeleteQueue).to(orderExchange).with("cart.delete");
    }

    // DLX Exchange
    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }

    // DLQ 큐
    @Bean
    public Queue cartDeleteDlq() {
        return new Queue(CART_DELETE_DLQ, true);
    }

    // DLQ → DLX 바인딩
    @Bean
    public Binding cartDeleteDlqBinding() {
        return BindingBuilder.bind(cartDeleteDlq()).to(dlxExchange()).with(CART_DELETE_QUEUE);
    }

    // 3회 재시도 후 DLQ 자동 라우팅
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setAdviceChain(
                RetryInterceptorBuilder.stateless()
                        .maxAttempts(3)
                        .backOffOptions(1000, 2.0, 10000)
                        .recoverer(new RejectAndDontRequeueRecoverer())
                        .build()
        );
        return factory;
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}
