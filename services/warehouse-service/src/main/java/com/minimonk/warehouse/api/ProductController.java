package com.minimonk.warehouse.api;

import com.minimonk.warehouse.ProductRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductRepository products;

    public ProductController(ProductRepository products) {
        this.products = products;
    }

    @GetMapping
    public List<ProductDto> listProducts() {
        return products.findAll().stream()
                .map(product -> new ProductDto(
                        product.getId(),
                        product.getSku(),
                        product.getName(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getAvailableQuantity(),
                        product.getReservedQuantity()))
                .toList();
    }
}
