package com.myproject.storemanager.controller;

import com.myproject.storemanager.api.request.ProductUpdateRequest;
import com.myproject.storemanager.exception.ProductCreateException;
import com.myproject.storemanager.exception.ProductDeleteException;
import com.myproject.storemanager.exception.ProductNotFoundException;
import com.myproject.storemanager.exception.ProductUpdateException;
import com.myproject.storemanager.model.Product;
import com.myproject.storemanager.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.findAll();
        logger.info("Retrieved {} products", products.size());
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        try {
            Product product = productService.findById(id);
            logger.info("Retrieved product: {}", product);
            return ResponseEntity.ok(product);
        } catch (ProductNotFoundException e) {
            logger.info("Product with ID {} not found", id);
            throw e;
        }
    }

    @PostMapping
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        try {
            Product createdProduct = productService.addProduct(product);
            logger.info("Created product with ID: {}", createdProduct.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
        } catch (ProductCreateException e) {
            logger.error("Product creation failed", e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody ProductUpdateRequest request) {
        try {
            Product updatedProduct = productService.updateProduct(id, request);
            logger.info("Updated product with ID: {}", updatedProduct.getId());
            return ResponseEntity.ok(updatedProduct);
        } catch (ProductUpdateException e) {
            logger.error("Product update failed", e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            logger.info("Deleted product with ID: {}", id);
            return ResponseEntity.ok("Product deleted successfully");
        } catch (ProductDeleteException e) {
            logger.error("Product deletion failed", e);
            throw e;
        }
    }
}
