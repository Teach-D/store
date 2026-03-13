package com.msa.product.domain.product.consumer;

import com.msa.product.domain.product.service.ProductService;
import com.msa.product.global.dto.ProductImageReadyEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductImageConsumer {

    private final ProductService productService;

    @RabbitListener(queues = "product.image.ready")
    public void handleProductImageReady(ProductImageReadyEvent event) {
        log.info("AI 이미지 생성 완료 수신 productId: {}", event.getProductId());
        try {
            productService.updateProductImages(
                    event.getProductId(),
                    event.getImageUrl(),
                    event.getPromoImageUrl()
            );
            log.info("이미지 URL DB 업데이트 완료 productId: {}", event.getProductId());
        } catch (Exception e) {
            log.error("이미지 URL 업데이트 실패 productId: {}", event.getProductId(), e);
            throw e;
        }
    }
}
