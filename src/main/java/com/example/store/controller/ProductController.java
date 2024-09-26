package com.example.store.controller;

import com.example.store.dto.request.RequestProduct;
import com.example.store.dto.response.ResponseDto;
import com.example.store.dto.response.ResponseProduct;
import com.example.store.dto.response.SuccessDto;
import com.example.store.entity.Product;
import com.example.store.jwt.util.IfLogin;
import com.example.store.jwt.util.LoginUserDto;
import com.example.store.service.OrderItemService;
import com.example.store.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final OrderItemService orderItemService;

/*    @GetMapping
    public ResponseDto<Page<Product>> getProducts(@RequestParam(required = false, defaultValue = "0") Long categoryId, @RequestParam(required = false, defaultValue = "0") int page) {
        return productService.getProducts(categoryId, page);
    }*/

    @GetMapping()
    public ResponseDto<Page<ResponseProduct>> getProductsByOption(
            @RequestParam(required = false, defaultValue = "0") Long categoryId, @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "0") String sort, @RequestParam(required = false, defaultValue = "0") String order
    ) {
        Page<ResponseProduct> responseProducts = null;
        Page<Product> products = null;

        log.info(sort + "-" + order);
        if (sort.equals("0") && order.equals("0")) {
            products = productService.getProducts(categoryId, page);
        } else if (sort.equals("sale")) {
            if (order.equals("asc")) {
                log.info("aa");
                products = productService.getProductsBySaleAsc(categoryId, page);
            } else {
                products = productService.getProductsBySaleDesc(categoryId, page);
            }
        } else if (sort.equals("price")) {
            if (order.equals("asc")) {
                log.info("aa");
                products = productService.getProductsByPriceAsc(categoryId, page);
            } else {
                products = productService.getProductsByPriceDesc(categoryId, page);
            }
        } else {
            products = productService.getProductsBySaleDesc(categoryId, page);
        }

        Page<ResponseProduct> resultProducts = new ResponseProduct().toDtoPage(products);
        return ResponseDto.success(resultProducts);
    }

    @GetMapping("/{id}")
    public ResponseDto<ResponseProduct> getProduct(@PathVariable Long id) {
        return productService.getProduct(id);
    }

    @GetMapping("/name/{name}")
    public ResponseDto<ResponseProduct> getProductByName(@PathVariable String name) {
        return productService.getProductByName(name);
    }

    @PostMapping
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseDto<ResponseProduct> addProduct(@IfLogin LoginUserDto loginUserDto, @RequestBody RequestProduct requestProduct) {
        return productService.addProduct(requestProduct);
    }

    @PutMapping("/{id}")
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<SuccessDto> editProduct(@RequestBody RequestProduct requestProduct, @PathVariable Long id) {
        return productService.editProduct(requestProduct, id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessDto> deleteProduct(@PathVariable Long id) {
        return productService.deleteProduct(id);
    }
}
