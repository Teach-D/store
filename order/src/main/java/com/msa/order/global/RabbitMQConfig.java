package com.msa.order.global;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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

    public static final String DLX_EXCHANGE = "dlx.exchange";
    public static final String PAYMENT_COMPLETED_DLQ = "payment.completed.dlq";
    public static final String PAYMENT_FAILED_DLQ = "payment.failed.dlq";


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
    public Binding binding(Queue orderQueue, @Qualifier("orderExchange") DirectExchange orderExchange) {
        return BindingBuilder.bind(orderQueue).to(orderExchange).with(ORDER_ROUTING_KEY);
    }

    @Bean
    public Binding orderCreatedPaymentBinding(Queue orderCreatedPaymentQueue, @Qualifier("orderExchange") DirectExchange orderExchange) {
          return BindingBuilder
                  .bind(orderCreatedPaymentQueue).to(orderExchange).with("order.created.payment");
    }

    @Bean
    public Queue paymentCompletedQueue() {
        return QueueBuilder.durable(PAYMENT_COMPLETED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", PAYMENT_COMPLETED_QUEUE)
                .build();
    }

    @Bean
    public Queue paymentFailedQueue() {
        return QueueBuilder.durable(PAYMENT_FAILED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", PAYMENT_FAILED_QUEUE)
                .build();
    }

    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(PAYMENT_EXCHANGE);
    }

    @Bean
    public Binding paymentCompletedBinding(Queue paymentCompletedQueue, TopicExchange paymentExchange) {
        return BindingBuilder.bind(paymentCompletedQueue).to(paymentExchange).with("payment.completed");
    }

    @Bean
    public Binding paymenFailedBinding(Queue paymentFailedQueue, TopicExchange paymentExchange) {
        return BindingBuilder.bind(paymentFailedQueue).to(paymentExchange).with("payment.failed");
    }

    @Bean
    public Queue stockRestoreQueue() {
        return new Queue(STOCK_RESTORE_QUEUE, true);
    }

    @Bean
    public Binding stockRestoreBinding(Queue stockRestoreQueue, @Qualifier("orderExchange") DirectExchange orderExchange) {
        return BindingBuilder.bind(stockRestoreQueue).to(orderExchange).with("stock.restore");
    }

    @Bean
    public Queue cartDeleteQueue() {
        return new Queue(CART_DELETE_QUEUE);
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
    public Queue paymentCompletedDlq() {
        return new Queue(PAYMENT_COMPLETED_DLQ, true);
    }

    @Bean
    public Queue paymentFailedDlq() {
        return new Queue(PAYMENT_FAILED_DLQ, true);
    }

    // DLQ → DLX 바인딩
    @Bean
    public Binding paymentCompletedDlqBinding() {
        return BindingBuilder.bind(paymentCompletedDlq()).to(dlxExchange()).with(PAYMENT_COMPLETED_QUEUE);
    }

    @Bean
    public Binding paymentFailedDlqBinding() {
        return BindingBuilder.bind(paymentFailedDlq()).to(dlxExchange()).with(PAYMENT_FAILED_QUEUE);
    }

    // 3회 재시도 후 DLQ 자동 라우팅
    // Prefetch: 소비자당 최대 미확인 메시지 수 제한 → 처리 지연 시 메시지 폭주 방지
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setPrefetchCount(10);
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
        // 라우팅 불가 메시지를 반환받기 위해 mandatory 활성화
        rabbitTemplate.setMandatory(true);
        // Publisher Returns: 라우팅 실패 메시지 감지 및 로깅
        rabbitTemplate.setReturnsCallback(returned ->
            log.error("[Publisher Returns] 라우팅 실패 - exchange={}, routingKey={}, replyCode={}, replyText={}",
                returned.getExchange(), returned.getRoutingKey(),
                returned.getReplyCode(), returned.getReplyText())
        );
        return rabbitTemplate;
    }
}
