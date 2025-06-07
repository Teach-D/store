package com.example.store.service;

import com.example.store.dto.request.RequestCartItem;
import com.example.store.dto.response.ResponseCartItem;
import com.example.store.dto.response.ResponseDto;
import com.example.store.dto.response.SuccessDto;
import com.example.store.entity.Cart;
import com.example.store.entity.CartItem;
import com.example.store.entity.Member;
import com.example.store.entity.product.Product;
import com.example.store.exception.ex.MemberException.NotFoundMemberException;
import com.example.store.exception.ex.NotFoundCartException;
import com.example.store.exception.ex.NotFoundCartItemException;
import com.example.store.exception.ex.ProductException.NotFoundProductException;
import com.example.store.exception.ex.ProductException.OutOfProductException;
import com.example.store.jwt.util.LoginUserDto;
import com.example.store.repository.CartItemRepository;
import com.example.store.repository.CartRepository;
import com.example.store.repository.MemberRepository;
import com.example.store.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CartItemService {

    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public ResponseDto<ResponseCartItem> getCartItem(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(NotFoundCartItemException::new);
        ResponseCartItem responseCartItem = ResponseCartItem.builder().quantity(cartItem.getQuantity()).productId(cartItem.getProduct().getId()).build();

        return ResponseDto.success(responseCartItem);
    }

    public ResponseDto<List<ResponseCartItem>> getCartItems(LoginUserDto loginUserDto) {
        Member member = memberRepository.findByEmail(loginUserDto.getEmail()).orElseThrow(NotFoundMemberException::new);
        Cart cart = cartRepository.findByMember(member).orElseThrow(NotFoundCartException::new);

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        for (CartItem cartItem : cartItems) {
            log.info("+++++++++++++++");

        }

        List<ResponseCartItem> cartItemDtos = new ArrayList<>();

        cartItems.forEach(cartItem -> {
            Product product = new Product(cartItem.getProduct());

            ResponseCartItem responseCartItem = ResponseCartItem
                    .builder().quantity(cartItem.getQuantity()).productId(product.getId()).id(cartItem.getId()).build();
            cartItemDtos.add(responseCartItem);
        });

        return ResponseDto.success(cartItemDtos);

    }

    public ResponseEntity<SuccessDto> addCartItem(LoginUserDto loginUserDto, RequestCartItem requestCartItem, Long productId) {
        Member member = memberRepository.findByEmail(loginUserDto.getEmail()).orElseThrow(NotFoundMemberException::new);
        Product product = productRepository.findById(productId).orElseThrow(NotFoundProductException::new);

        int orderQuantity = requestCartItem.getQuantity();

        if (product.getQuantity() < orderQuantity) {
            throw new OutOfProductException();
        }


        if (cartItemRepository.existsByCartIdAndProductId(requestCartItem.getCartId(), productId)) {
            CartItem cartItem = cartItemRepository.findCartItemByCartIdAndProductId(requestCartItem.getCartId(), productId).orElseThrow(NotFoundCartItemException::new);
            cartItem.updateQuantity(cartItem.getQuantity() + requestCartItem.getQuantity());
            product.updateQuantity(product.getQuantity() - requestCartItem.getQuantity());
            product.updateSaleQuantity(product.getSaleQuantity() + requestCartItem.getQuantity());
            return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
            //updateCartItem(cartItem);
        }

        product.updateQuantity(product.getQuantity() - requestCartItem.getQuantity());
        product.updateSaleQuantity(product.getSaleQuantity() + requestCartItem.getQuantity());

        Cart cart = cartRepository.findById(requestCartItem.getCartId()).orElseThrow(NotFoundCartException::new);

        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .quantity(requestCartItem.getQuantity())
                .product(product)
                .build();

        CartItem save = cartItemRepository.save(cartItem);

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }

    public ResponseEntity<SuccessDto> editCartItem(Long cartItemId, int quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(NotFoundCartItemException::new);

        int originalQuantity = cartItem.getQuantity();

        cartItem.updateQuantity(quantity);

        Product product = cartItem.getProduct();
        product.updateQuantity(product.getQuantity() + originalQuantity - quantity);
        product.updateSaleQuantity(product.getSaleQuantity() - originalQuantity + quantity);

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }

    public ResponseEntity<SuccessDto> deleteCartItem(LoginUserDto loginUserDto, Long cartItemId) {
        Member member = memberRepository.findByEmail(loginUserDto.getEmail()).orElseThrow(NotFoundMemberException::new);

        Cart cart = cartRepository.findByMember(member).orElseThrow(NotFoundCartException::new);
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(NotFoundCartItemException::new);

        Product product = productRepository.findById(cartItem.getProduct().getId()).orElseThrow(NotFoundProductException::new);

        if (isCartItemExistByCartId(cart.getId(), cartItemId) == false)
            return ResponseEntity.badRequest().build();

        product.updateQuantity(product.getQuantity() + cartItem.getQuantity());
        product.updateSaleQuantity(product.getSaleQuantity() - cartItem.getQuantity());
        cartItemRepository.deleteById(cartItemId);

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }

    @Transactional
    public void deleteCartItem(Long memberId, Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    @Transactional(readOnly = true)
    public List<CartItem> getCartItemsByCartId(Long cartId) {
        return cartItemRepository.findCartItemByCartId(cartId);
    }

    public boolean isCartItemExistByCartId(Long id, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow();
        return (cartItem.getCart().getId() == id);
    }
}
