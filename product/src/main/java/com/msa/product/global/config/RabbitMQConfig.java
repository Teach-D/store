package com.msa.product.global.config;

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

    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String STOCK_RESTORE_QUEUE = "stock.restore";

    // AI 이미지 생성 관련
    public static final String PRODUCT_EXCHANGE = "product.exchange";
    public static final String PRODUCT_CREATED_QUEUE = "product.created";
    public static final String PRODUCT_IMAGE_READY_QUEUE = "product.image.ready";

    // DLX / DLQ
    public static final String DLX_EXCHANGE = "dlx.exchange";
    public static final String STOCK_RESTORE_DLQ = "stock.restore.dlq";
    public static final String PRODUCT_IMAGE_READY_DLQ = "product.image.ready.dlq";


    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE);
    }

    @Bean
    public DirectExchange productExchange() {
        return new DirectExchange(PRODUCT_EXCHANGE);
    }

    @Bean
    public Queue productCreatedQueue() {
        return QueueBuilder.durable(PRODUCT_CREATED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", PRODUCT_CREATED_QUEUE)
                .build();
    }

    @Bean
    public Queue productImageReadyQueue() {
        return QueueBuilder.durable(PRODUCT_IMAGE_READY_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", PRODUCT_IMAGE_READY_QUEUE)
                .build();
    }

    @Bean
    public Binding productCreatedBinding(Queue productCreatedQueue, @Qualifier("productExchange") DirectExchange productExchange) {
        return BindingBuilder.bind(productCreatedQueue).to(productExchange).with("product.created");
    }

    @Bean
    public Binding productImageReadyBinding(Queue productImageReadyQueue, @Qualifier("productExchange") DirectExchange productExchange) {
        return BindingBuilder.bind(productImageReadyQueue).to(productExchange).with("product.image.ready");
    }

    @Bean
    public Queue productImageReadyDlq() {
        return new Queue(PRODUCT_IMAGE_READY_DLQ, true);
    }

    @Bean
    public Binding productImageReadyDlqBinding() {
        return BindingBuilder.bind(productImageReadyDlq()).to(dlxExchange()).with(PRODUCT_IMAGE_READY_QUEUE);
    }

    // stock.restore — DLX 연결
    @Bean
    public Queue stockRestoreQueue() {
        return QueueBuilder.durable(STOCK_RESTORE_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", STOCK_RESTORE_QUEUE)
                .build();
    }

    @Bean
    public Binding stockRestoreBinding(Queue stockRestoreQueue, @Qualifier("orderExchange") DirectExchange orderExchange) {
        return BindingBuilder.bind(stockRestoreQueue).to(orderExchange).with("stock.restore");
    }

    // DLX Exchange
    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }

    // DLQ 큐
    @Bean
    public Queue stockRestoreDlq() {
        return new Queue(STOCK_RESTORE_DLQ, true);
    }

    // DLQ → DLX 바인딩
    @Bean
    public Binding stockRestoreDlqBinding() {
        return BindingBuilder.bind(stockRestoreDlq()).to(dlxExchange()).with(STOCK_RESTORE_QUEUE);
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
