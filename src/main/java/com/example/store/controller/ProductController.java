package com.example.store.controller;

import com.example.store.dto.request.RequestProduct;
import com.example.store.dto.response.ResponseDto;
import com.example.store.dto.response.ResponseProduct;
import com.example.store.dto.response.SuccessDto;
import com.example.store.entity.product.Product;
import com.example.store.jwt.util.IfLogin;
import com.example.store.jwt.util.LoginUserDto;
import com.example.store.service.OrderItemService;
import com.example.store.service.ProductCacheService;
import com.example.store.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final OrderItemService orderItemService;
    private final ProductCacheService productCacheService;
/*    @GetMapping
    public ResponseDto<Page<Product>> getProducts(@RequestParam(required = false, defaultValue = "0") Long categoryId, @RequestParam(required = false, defaultValue = "0") int page) {
        return productService.getProducts(categoryId, page);
    }*/

/*    @GetMapping()
    public ResponseDto<Page<ResponseProduct>> getProductsByOption(
            @RequestParam(required = false, defaultValue = "0") Long categoryId, @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "0") String sort, @RequestParam(required = false, defaultValue = "0") String order
    ) {
        Page<ResponseProduct> responseProducts = null;
        Page<Product> products = null;

        if (sort.equals("0") && order.equals("0")) {
            products = productService.getProducts(categoryId, page);
        } else if (sort.equals("sale")) {
            if (order.equals("asc")) {
                products = productService.getProductsBySaleAsc(categoryId, page);
            } else {
                products = productService.getProductsBySaleDesc(categoryId, page);
            }
        } else if (sort.equals("price")) {
            if (order.equals("asc")) {
                products = productService.getProductsByPriceAsc(categoryId, page);
            } else {
                products = productService.getProductsByPriceDesc(categoryId, page);
            }
        } else {
            products = productService.getProductsBySaleDesc(categoryId, page);
        }

        Page<ResponseProduct> resultProducts = new ResponseProduct().toDtoPage(products);
        return ResponseDto.success(resultProducts);
    }*/

    @GetMapping
    public ResponseEntity<List<ResponseProduct>> getProductsByOption(
            @RequestParam(required = false, defaultValue = "0") Long categoryId, @RequestParam(required = false, defaultValue = "0") String title,
            @RequestParam(required = false, defaultValue = "0") String sort, @RequestParam(required = false, defaultValue = "0") String order
    ) {
        return ResponseEntity.status(OK).body(productService.getProductsByOption(categoryId, title, sort, order));
    }



    @GetMapping("/{id}")
    public ResponseDto<ResponseProduct> getProduct(@PathVariable Long id) {
        return productService.getProduct(id);
    }

    @GetMapping("/name/{name}")
    public ResponseDto<ResponseProduct> getProductByName(@PathVariable String name) {
        return productService.getProductByName(name);
    }

    // categoryId에 속해 있는 product list 조회
    @GetMapping("/category/{categoryId}")
    public ResponseDto<List<ResponseProduct>> getProductByCategoryId(@PathVariable Long categoryId) {
        return productService.getProductsByCategoryId(categoryId);
    }

    // tagId에 속해 있는 product list 조회
    @GetMapping("/tag/{tagId}")
    public ResponseDto<List<ResponseProduct>> getProductByTagId(@PathVariable Long tagId) {
        return productService.getProductsByTagId(tagId);
    }

    // tagId에 속해 있는 cached product list 조회(하루동안 특정 조회수보다 적은 조회수면 DB에서 product list 조회)
    @GetMapping("/cached/tag/{tagId}")
    public ResponseDto<List<ResponseProduct>> getCachedProductByTagId(@PathVariable Long tagId) {
        return productCacheService.getPopularProductsByTagId(tagId);
    }

    @PostMapping
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity addProduct(@IfLogin LoginUserDto loginUserDto, @Valid @RequestBody RequestProduct requestProduct, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult.getAllErrors().forEach(objectError -> {
                String message = objectError.getDefaultMessage();
                sb.append("message :" + message);
            });
            return ResponseEntity.status(BAD_REQUEST).body(sb.toString());
        }
        return productService.addProduct(requestProduct);
    }

    @PatchMapping("/{id}")
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity editProduct(@Valid @RequestBody RequestProduct requestProduct, BindingResult bindingResult, @PathVariable Long id) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult.getAllErrors().forEach(objectError -> {
                String message = objectError.getDefaultMessage();
                sb.append("message :" + message);
            });
            return ResponseEntity.status(BAD_REQUEST).body(sb.toString());
        }
        return productService.editProduct(requestProduct, id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessDto> deleteProduct(@PathVariable Long id) {
        return productService.deleteProduct(id);
    }
}
