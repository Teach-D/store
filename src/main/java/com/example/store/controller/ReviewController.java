package com.example.store.controller;

import com.example.store.dto.request.RequestReview;
import com.example.store.dto.response.ResponseDto;
import com.example.store.dto.response.ResponseReview;
import com.example.store.dto.response.SuccessDto;
import com.example.store.entity.Member;
import com.example.store.entity.product.Product;
import com.example.store.jwt.util.IfLogin;
import com.example.store.jwt.util.LoginUserDto;
import com.example.store.repository.ProductRepository;
import com.example.store.service.MemberService;
import com.example.store.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;
    private final MemberService memberService;
    private final ProductRepository productRepository;

    @GetMapping("/{id}")
    public ResponseDto<ResponseReview> getReview(@PathVariable Long id) {
        return reviewService.getReviewById(id);
    }

    @GetMapping
    public ResponseDto<Page<ResponseReview>> getReviewsByProduct(
            @RequestParam(required = false, defaultValue = "0") Long productId, @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "0") String sort, @RequestParam(required = false, defaultValue = "0") String order
    ) {
        Page<ResponseReview> reviews = reviewService.getByProductId(productId, page);
        return ResponseDto.success(reviews);
    }

    @PostMapping("/{productId}")
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity addReview(@IfLogin LoginUserDto loginUserDto, @Valid @RequestBody RequestReview requestReview, BindingResult bindingResult, @PathVariable Long productId) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult.getAllErrors().forEach(objectError -> {
                String message = objectError.getDefaultMessage();
                sb.append("message :" + message);
            });
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(sb.toString());
        }

        Member member = memberService.findByEmail(loginUserDto.getEmail());
        Product product = productRepository.findById(productId).orElseThrow(RuntimeException::new);

        return reviewService.createReview(member, product, requestReview);
    }

    @PatchMapping("/{id}")
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity editReview(@Valid @RequestBody RequestReview requestReview, BindingResult bindingResult, @PathVariable Long id) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult.getAllErrors().forEach(objectError -> {
                String message = objectError.getDefaultMessage();
                sb.append("message :" + message);
            });
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(sb.toString());
        }
        return reviewService.updateReview(id, requestReview);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessDto> deleteProduct(@PathVariable Long id) {
        return reviewService.deleteReview(id);
    }
}
