package com.msa.product.domain.product.consumer;

import com.msa.product.domain.product.dto.StockRestoreEvent;
import com.msa.product.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockRestoreConsumer {

    private final ProductService productService;

    @RabbitListener(queues = "stock.restore")
    public void handleStockRestore(StockRestoreEvent event) {
        log.info("store restore 보상 트랜잭션 수신 orderId : {}, items : {}", event.getOrderId(), event.getItems().size());

        try {
            for (StockRestoreEvent.StockRestoreItem item : event.getItems()) {
                productService.restoreTock(item.getProductId(), item.getQuantity());
                log.info("재고 복구 productId : {}, quentity : {}",  item.getProductId(), item.getQuantity());
            }

            log.info("재고 복구 완료 orderId : {}",  event.getOrderId());
        } catch (Exception e) {
            log.error("재고 복구 실패 orderId : {}",  event.getOrderId());
            throw e;
        }
    }
}
