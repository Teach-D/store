package com.example.store.repository;

import com.example.store.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findCartItemByCartIdAndProductId(Long cartId, Long productId);

    boolean existsByCartIdAndProductId(Long cartId, Long productId);

    List<CartItem> findCartItemByCartId(Long cartId);


    List<CartItem> findByCartId(Long cartId);
}
