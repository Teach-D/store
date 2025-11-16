package com.msa.product.domain.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestProduct {

    @NotBlank(message = "제품명을 적어주세요")
    private String title;

    @NotNull(message = "가격을 적어주세요")
    private int price;

    @NotBlank(message = "제품설명을 적어주세요")
    private String description;

    @NotNull(message = "제품이 어느 카테고리에 속하는지 적어주세요")
    private Long categoryId;

    private String imageUrl;

    @NotNull(message = "제품의 수량을 적어주세요")
    private int quantity;
}
