package com.msa.product.domain.tag.entity;

import com.msa.product.domain.tag.dto.request.RequestTag;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "tag", orphanRemoval = true)
    private final List<ProductTag> productTags = new ArrayList<>();

    public void updateTag(RequestTag requestTag) {
        this.name = requestTag.getName();
    }
}
