package com.example.store.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
@Setter
public class OrderItem {

    @Id
    @Column(name = "order_item_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "order_id")
    private Order order;

    private String productTitle;
    private int productPrice;
    int quantity;

    public void deleteProduct() {
        this.product = null;
    }

    public void updateProduct() {
        this.productTitle = this.product.getTitle();
        this.productPrice = this.product.getPrice();

    }
}
