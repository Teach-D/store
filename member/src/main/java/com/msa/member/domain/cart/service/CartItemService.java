package com.msa.member.domain.cart.service;

import com.msa.member.common.client.ProductServiceClient;
import com.msa.member.domain.cart.dto.CartItemDto;
import com.msa.member.domain.cart.dto.request.RequestCartItem;
import com.msa.member.domain.cart.dto.response.ResponseCartItem;
import com.msa.member.domain.cart.entity.Cart;
import com.msa.member.domain.cart.entity.CartItem;
import com.msa.member.domain.cart.repository.CartItemRepository;
import com.msa.member.domain.cart.repository.CartRepository;
import com.msa.member.domain.member.entity.Member;
import com.msa.member.domain.member.repository.MemberRepository;
import com.msa.member.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.msa.member.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CartItemService {

    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final MemberRepository memberRepository;
    private final ProductServiceClient productServiceClient;

    public ResponseCartItem findCartItem(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(() -> new CustomException(CART_ITEM_NOT_FOUND));
        return ResponseCartItem.builder().quantity(cartItem.getQuantity()).productId(cartItem.getProductId()).build();
    }

    public List<ResponseCartItem> findCartItems(Long userId) {
        Member member = memberRepository.findById(userId).orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
        Cart cart = cartRepository.findByMember(member).orElseThrow(() -> new CustomException(CART_NOT_FOUND));

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());

        List<ResponseCartItem> cartItemDtos = new ArrayList<>();

        cartItems.forEach(cartItem -> {
            ResponseCartItem responseCartItem = ResponseCartItem
                    .builder().quantity(cartItem.getQuantity()).productId(cartItem.getProductId()).id(cartItem.getId()).build();
            cartItemDtos.add(responseCartItem);
        });

        return cartItemDtos;
    }

    public void addCartItem(RequestCartItem requestCartItem, Long productId) {
        int orderQuantity = requestCartItem.getQuantity();
        int productQuantity = productServiceClient.getProductQuantity(productId);
        int productSaleQuantity = productServiceClient.getProductSaleQuantity(productId);

        if (productQuantity < orderQuantity) {
            throw new CustomException(OUT_OF_PRODUCT_QUANTITY);
        }

        if (cartItemRepository.existsByCartIdAndProductId(requestCartItem.getCartId(), productId)) {
            CartItem cartItem = cartItemRepository
                    .findCartItemByCartIdAndProductId(requestCartItem.getCartId(), productId)
                    .orElseThrow(() -> new CustomException(CART_ITEM_NOT_FOUND));

            cartItem.updateQuantity(cartItem.getQuantity() + requestCartItem.getQuantity());
/*
            productServiceClient.updateProductQuantity(productId, productQuantity - orderQuantity);
            productServiceClient.updateProductSaleQuantity(productId, productSaleQuantity + orderQuantity);
*/

            return;
        }

/*
        productServiceClient.updateProductQuantity(productId, productQuantity - orderQuantity);
        productServiceClient.updateProductSaleQuantity(productId, productSaleQuantity + orderQuantity);
*/

        Cart cart = cartRepository.findById(requestCartItem.getCartId()).orElseThrow(() -> new CustomException(CART_NOT_FOUND));

        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .quantity(requestCartItem.getQuantity())
                .productId(productId)
                .build();

        cartItemRepository.save(cartItem);
    }


    @Transactional
    public void deleteCartItem(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(() -> new CustomException(CART_ITEM_NOT_FOUND));
        int quantity = cartItem.getQuantity();
        Long productId = cartItem.getProductId();

        int productQuantity = productServiceClient.getProductQuantity(productId);
        int productSaleQuantity = productServiceClient.getProductSaleQuantity(productId);

        productServiceClient.updateProductQuantity(productId, productQuantity + quantity);
        productServiceClient.updateProductSaleQuantity(productId, productSaleQuantity - quantity);

        cartItemRepository.deleteById(cartItemId);

    }

    @Transactional
    public void clearCartItem(Long cartItemId) {


        cartItemRepository.deleteById(cartItemId);
    }

    public List<CartItemDto> getCartItemDtos(Long cartId) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(() -> new CustomException(CART_NOT_FOUND));
        return cart.getCartItemList().stream()
                .map(cartItem -> CartItemDto.builder().cartItemId(cartItem.getId()).productId(cartItem.getProductId()).productPrice(productServiceClient.getProductPrice(cartItem.getProductId())).quantity(cartItem.getQuantity()).build())
                .toList();
    }
}
