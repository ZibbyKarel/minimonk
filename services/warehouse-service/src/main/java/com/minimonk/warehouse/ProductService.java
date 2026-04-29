package com.minimonk.warehouse;

import com.minimonk.warehouse.api.ProductDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository products;

    public ProductService(ProductRepository products) {
        this.products = products;
    }

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
