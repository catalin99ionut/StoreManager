package com.myproject.storemanager.service;

import com.myproject.storemanager.api.request.ProductUpdateRequest;
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

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAll() {
        logger.info("Fetching all products");
        return productRepository.findAll();
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> {
                    logger.info("Product with ID {} doesn't exist", id);
                    return new ProductNotFoundException(id);
                });
    }

    public Product addProduct(Product product) {
        logger.info("Adding product with ID: {}", product.getId());
        try {
            Product savedProduct = productRepository.save(product);
            logger.info("Created new product: {}", savedProduct);
            return savedProduct;
        } catch (Exception e) {
            logger.error("Error while adding product: {} - {}", product, e.getMessage());
            throw new ProductCreateException("Error while creating product", e);
        }
    }

    public Product updateProduct(Long id, ProductUpdateRequest request) {
        logger.info("Updating product: {}", request);
        Product product = findById(id);

        try {
            if (request.getName() != null) {
                product.setName(request.getName());
                logger.info("Updated product name to: {}", request.getName());
            }
            if (request.getPrice() != null) {
                product.setPrice(request.getPrice());
                logger.info("Updated product price to: {}", request.getPrice());
            }
            return productRepository.save(product);
        } catch (Exception e) {
            logger.error("Error while updating product with ID: {} - {}", product.getId(), e.getMessage());
            throw new ProductUpdateException("Error while updating product", e);
        }
    }

    public void deleteProduct(Long id) {
        logger.info("Deleting product with ID: {}", id);

        if (!productRepository.existsById(id)) {
            logger.info("Cannot delete. Product with ID {} doesn't exist", id);
            throw new ProductNotFoundException(id);
        }

        try {
            productRepository.deleteById(id);
            logger.info("Successfully deleted product with ID: {}", id);
        } catch (Exception e) {
            logger.error("Error while deleting product with ID: {} - {}", id, e.getMessage());
            throw new ProductDeleteException("Error while deleting product", e);
        }
    }
}
