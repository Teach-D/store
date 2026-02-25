package com.msa.product.domain.product.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.msa.product.domain.category.entity.Category;
import com.msa.product.domain.review.entity.Review;
import com.msa.product.domain.tag.entity.ProductTag;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(
    name = "product",
    indexes = {
        @Index(name = "idx_product_category_sale", columnList = "category_id, saleQuantity DESC, id"),  // 카테고리별 판매순 No-Offset 커버링 인덱스
        @Index(name = "idx_product_title", columnList = "title"),
        @Index(name = "idx_product_sale_quantity", columnList = "saleQuantity DESC, id")                // 전체 판매순 No-Offset 커버링 인덱스
    }
)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sellerId;

    private String title;

    private int price;

    private int quantity;

    private int saleQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product")
    private final List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "product", orphanRemoval = true)
    @JsonIgnore
    private final List<ProductTag> productTags = new ArrayList<>();

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private ProductDetail productDetail;

    public Product(Product p) {
        this.id = p.getId();
        this.title = p.getTitle();
        this.price = p.getPrice();
        this.quantity = p.getQuantity();
        this.category = p.getCategory();
        this.saleQuantity = p.getSaleQuantity();
        this.productDetail = p.getProductDetail();
    }

    public void updateQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void updateSaleQuantity(int saleQuantity) {
        this.saleQuantity = saleQuantity;
    }

    public void updateProduct(Category category, int price, String description, String imageUrl, String title, int quantity) {
        this.category = category;
        this.price = price;
        this.title = title;
        this.quantity = quantity;
    }
}
