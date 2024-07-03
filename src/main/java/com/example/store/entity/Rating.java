package com.example.store.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Rating {
    private Double rate;
    private Integer count;
}
