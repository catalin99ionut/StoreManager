package com.myproject.storemanager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.storemanager.api.request.ProductRequest;
import com.myproject.storemanager.exception.ProductCreateException;
import com.myproject.storemanager.exception.ProductDeleteException;
import com.myproject.storemanager.exception.ProductNotFoundException;
import com.myproject.storemanager.exception.ProductUpdateException;
import com.myproject.storemanager.model.Product;
import com.myproject.storemanager.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    public ProductService(ProductRepository productRepository, ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.objectMapper = objectMapper;
    }

    public List<Product> findAll() {
        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) {
            throw new ProductNotFoundException("There are no products in the database");
        }
        logger.info("Fetched all products");
        return products;
    }

    public Product findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        try {
            String productJson = objectMapper.writeValueAsString(product);
            logger.info("Retrieved product: {}", productJson);
            return product;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Product> findByName(String name) {
        List<Product> products = productRepository.findByNameContainingIgnoreCase(name);
        if (products.isEmpty()) {
            throw new ProductNotFoundException("No results for: " + name);
        }
        try {
            String productsJson = objectMapper.writeValueAsString(products);
            logger.info("Retrieved products: {}", productsJson);
            return products;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Product addProduct(ProductRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new ProductCreateException("Product name cannot be empty");
        }
        if (request.getPrice() == null) {
            throw new ProductCreateException("Product price cannot be empty");
        }

        Product product = new Product(request.getName(), request.getPrice());

        try {
            Product savedProduct = productRepository.save(product);
            String productJson = objectMapper.writeValueAsString(savedProduct);
            logger.info("Successfully created product: {}", productJson);
            return savedProduct;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new ProductCreateException("Error while creating product", e);
        }
    }

    public Product updateProduct(Long id, ProductRequest request) {
        Product product = findById(id);

        try {
            if (request.getName() != null) {
                product.setName(request.getName());
            }
            if (request.getPrice() != null) {
                product.setPrice(request.getPrice());
            }
            Product updatedProduct = productRepository.save(product);
            String productJson = objectMapper.writeValueAsString(updatedProduct);
            logger.info("Successfully updated product with ID {}. New fields: {}", updatedProduct.getId(), productJson);
            return updatedProduct;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new ProductUpdateException("Error while updating product with ID: " + product.getId(), e);
        }
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Cannot delete. Product with ID " + id + " doesn't exist");
        }

        try {
            productRepository.deleteById(id);
            logger.info("Successfully deleted product with ID: {}", id);
        } catch (Exception e) {
            throw new ProductDeleteException("Error while deleting product with ID " + id, e);
        }
    }
}
