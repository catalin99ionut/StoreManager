package com.myproject.storemanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.storemanager.api.request.ProductRequest;
import com.myproject.storemanager.model.Product;
import com.myproject.storemanager.repository.ProductRepository;
import com.myproject.storemanager.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class ProductControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private ProductService productService;
    private Product product;
    private ObjectMapper objectMapper;
    @Autowired
    private ProductController productController;

    @BeforeEach
    public void setup() {
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(100.0);

        objectMapper = new ObjectMapper();
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    public void testGetAllProducts() throws Exception {
        when(productService.findAll()).thenReturn(Collections.singletonList(product));
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Product"))
                .andExpect(jsonPath("$[0].price").value(100.0));
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    public void testGetProductById() throws Exception {
        when(productService.findById(product.getId())).thenReturn(product);
        mockMvc.perform(get("/products/id/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(100.0));
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    public void testGetProductByName() throws Exception {
        when(productService.findByName(product.getName())).thenReturn(Collections.singletonList(product));
        mockMvc.perform(get("/products/name/" + product.getName()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Product"))
                .andExpect(jsonPath("$[0].price").value(100.0));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testCreateProduct_withPermittedUser() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName(product.getName());
        request.setPrice(product.getPrice());

        when(productService.addProduct(any(ProductRequest.class))).thenAnswer(invocationOnMock -> {
            ProductRequest productRequest = invocationOnMock.getArgument(0);
            Product savedProduct = new Product();
            savedProduct.setId(product.getId());
            savedProduct.setName(productRequest.getName());
            savedProduct.setPrice(productRequest.getPrice());
            return savedProduct;
        });

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(100.0));
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    public void testCreateProduct_withForbiddenUser() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Product\",\"price\":100.0}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testUpdateProduct_withPermittedUser() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("Updated Name");
        request.setPrice(110.0);

        Product updatedProduct = new Product();
        updatedProduct.setId(product.getId());
        updatedProduct.setName("Updated Name");
        updatedProduct.setPrice(110.0);

        when(productService.updateProduct(eq(product.getId()), any(ProductRequest.class))).thenReturn(updatedProduct);
        when(productService.findById(product.getId())).thenReturn(product);

        mockMvc.perform(patch("/products/" + product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value(updatedProduct.getName()))
                .andExpect(jsonPath("$.price").value(updatedProduct.getPrice()));
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    public void testUpdateProduct_withForbiddenUser() throws Exception {
        mockMvc.perform(patch("/products/" + product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Product\",\"price\":100.0}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testDeleteProduct_withPermittedUser() throws Exception {
        when(productRepository.existsById(product.getId())).thenReturn(true);
        doNothing().when(productRepository).deleteById(product.getId());

        mockMvc.perform(delete("/products/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("Product deleted successfully"));
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    public void testDeleteProduct_withForbiddenUser() throws Exception {
        mockMvc.perform(delete("/products/" + product.getId()))
                .andExpect(status().isForbidden());
    }
}
