package com.msa.product.domain.category.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestCategory {

    @NotBlank(message = "카테고리명을 반드시 입력해주세요")
    private String name;
}