package com.myproject.storemanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.storemanager.api.request.ProductRequest;
import com.myproject.storemanager.exception.ProductNotFoundException;
import com.myproject.storemanager.model.Product;
import com.myproject.storemanager.repository.ProductRepository;
import com.myproject.storemanager.service.ProductService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreManagerApplicationTests {

	@Mock
	private ProductRepository productRepository;

	@InjectMocks
	private ProductService productService;
    private Product product;

	@BeforeEach
	public void setup() {
		productService = new ProductService(productRepository, new ObjectMapper());

		product = new Product();
		product.setId(1L);
		product.setName("Test Product");
		product.setPrice(100.0);
	}

	@Test
	public void testAddProduct() {
		ProductRequest request = new ProductRequest();
		request.setName(product.getName());
		request.setPrice(product.getPrice());

		when(productRepository.save(any(Product.class))).thenAnswer(invocationOnMock -> {
			Product savedProduct = invocationOnMock.getArgument(0);
			savedProduct.setId(product.getId());
			return savedProduct;
		});

		Product result = productService.addProduct(request);

		Assertions.assertEquals(product.getId(), result.getId());
		Assertions.assertEquals(product.getName(), result.getName());
		Assertions.assertEquals(product.getPrice(), result.getPrice());
		verify(productRepository, times(1)).save(any(Product.class));
	}

	@Test
	public void testFindAllProducts() {
		List<Product> products = Collections.singletonList(product);
		when(productRepository.findAll()).thenReturn(products);

		List<Product> result = productService.findAll();

		Assertions.assertNotNull(result);
		Assertions.assertEquals(products.size(), result.size());
		Assertions.assertEquals(product.getId(), result.get(0).getId());
		Assertions.assertEquals(product.getName(), result.get(0).getName());
		Assertions.assertEquals(product.getPrice(), result.get(0).getPrice());

		verify(productRepository, times(1)).findAll();

		Product secondProduct = new Product();
		secondProduct.setId(2L);
		secondProduct.setName("Test Product 2");
		secondProduct.setPrice(100.0);

		products = Arrays.asList(product, secondProduct);
		when(productRepository.findAll()).thenReturn(products);

		result = productService.findAll();

		Assertions.assertNotNull(result);
		Assertions.assertEquals(products.size(), result.size());
		Assertions.assertEquals(secondProduct.getId(), result.get(1).getId());
		Assertions.assertEquals(secondProduct.getName(), result.get(1).getName());
		Assertions.assertEquals(secondProduct.getPrice(), result.get(1).getPrice());

		verify(productRepository, times(2)).findAll();
	}

	@Test
	public void testFindProductById_whenIdExists() {
		when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

		Product result = productService.findById(product.getId());

		Assertions.assertEquals(product.getId(), result.getId());
		Assertions.assertEquals(product.getName(), result.getName());
		Assertions.assertEquals(product.getPrice(), result.getPrice());
		verify(productRepository, times(1)).findById(product.getId());
	}

	@Test
	public void testFindProductById_whenIdDoesNotExist() {
		when(productRepository.findById(product.getId())).thenReturn(Optional.empty());

		ProductNotFoundException exception = Assertions
				.assertThrows(ProductNotFoundException.class, () -> productService.findById(product.getId()));

		Assertions.assertEquals("Product with ID " + product.getId() + " was not found",
				exception.getMessage());
		verify(productRepository, times(1)).findById(product.getId());
	}

	@Test
	public void testFindByName_whenThereAreProductsWithNameContainingKeyword() {
		String keyword = "test";
		List<Product> productsSingleton = Collections.singletonList(product);
		when(productRepository.findByNameContainingIgnoreCase(keyword)).thenReturn(productsSingleton);

		List<Product> result = productService.findByName(keyword);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(productsSingleton.size(), result.size());
		Assertions.assertEquals(product.getId(), result.get(0).getId());
		Assertions.assertEquals(product.getName(), result.get(0).getName());
		Assertions.assertEquals(product.getPrice(), result.get(0).getPrice());

		verify(productRepository, times(1)).findByNameContainingIgnoreCase(keyword);

		Product secondProduct = new Product();
		secondProduct.setId(2L);
		secondProduct.setName("Product 2");
		secondProduct.setPrice(100.0);

		Product thirdProduct = new Product();
		thirdProduct.setId(3L);
		thirdProduct.setName("Test Product 3");
		thirdProduct.setPrice(100.0);

		List<Product> products = Arrays.asList(product, secondProduct, thirdProduct);
		when(productRepository.findByNameContainingIgnoreCase(keyword)).thenAnswer(invocationOnMock -> {
			String keywordArgument = invocationOnMock.getArgument(0);
			return products.stream()
					.filter(p -> p.getName().toLowerCase().contains(keywordArgument.toLowerCase()))
					.collect(Collectors.toList());
		});

		result = productService.findByName(keyword);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(products.size() - 1, result.size());
		Assertions.assertEquals(thirdProduct.getId(), result.get(1).getId());
		Assertions.assertEquals(thirdProduct.getName(), result.get(1).getName());
		Assertions.assertEquals(thirdProduct.getPrice(), result.get(1).getPrice());

		verify(productRepository, times(2)).findByNameContainingIgnoreCase(keyword);
	}

	@Test
	public void testFindByName_whenThereAreNoProductsWithNameContainingKeyword() {
		String keyword = "best";
		when(productRepository.findByNameContainingIgnoreCase(keyword)).thenReturn(Collections.emptyList());

		ProductNotFoundException exception = Assertions
				.assertThrows(ProductNotFoundException.class, () -> productService.findByName(keyword));

		Assertions.assertEquals("No results for: " + keyword, exception.getMessage());
		verify(productRepository, times(1)).findByNameContainingIgnoreCase(keyword);
	}

	@Test
	public void testUpdateProduct_withMultipleFieldChanges_whenIdExists() {
		when(productRepository.save(any(Product.class))).thenReturn(product);
		when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

		ProductRequest request = new ProductRequest();
		request.setName("Updated Name");
		request.setPrice(110.0);

		Product result = productService.updateProduct(product.getId(), request);

		Assertions.assertEquals(product.getId(), result.getId());
		Assertions.assertEquals(request.getName(), result.getName());
		Assertions.assertEquals(request.getPrice(), result.getPrice());
		verify(productRepository, times(1)).findById(product.getId());
		verify(productRepository, times(1)).save(product);
	}

	@Test
	public void testUpdateProduct_withOneFieldChange_whenIdExists() {
		when(productRepository.save(any(Product.class))).thenReturn(product);
		when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

		ProductRequest request = new ProductRequest();
		request.setName("Updated Name");

		Product result = productService.updateProduct(product.getId(), request);

		Assertions.assertEquals(product.getId(), result.getId());
		Assertions.assertEquals(request.getName(), result.getName());
		Assertions.assertEquals(product.getPrice(), result.getPrice());
		verify(productRepository, times(1)).findById(product.getId());
		verify(productRepository, times(1)).save(product);
	}

	@Test
	public void testUpdateProduct_whenIdDoesNotExist() {
		when(productRepository.findById(product.getId())).thenReturn(Optional.empty());

		ProductNotFoundException exception = Assertions
				.assertThrows(ProductNotFoundException.class,
						() -> productService.updateProduct(product.getId(), new ProductRequest()));

		Assertions.assertEquals("Product with ID " + product.getId() + " was not found",
				exception.getMessage());
		verify(productRepository, times(1)).findById(product.getId());
		verify(productRepository, never()).save(product);
	}

	@Test
	public void testDeleteProduct_whenIdExists() {
		when(productRepository.existsById(product.getId())).thenReturn(true);

		productService.deleteProduct(product.getId());

		verify(productRepository, times(1)).existsById(product.getId());
		verify(productRepository, times(1)).deleteById(product.getId());
	}

	@Test
	public void testDeleteProduct_whenIdDoesNotExist() {
		when(productRepository.existsById(product.getId())).thenReturn(false);

		Assertions.assertThrows(ProductNotFoundException.class, () -> productService.deleteProduct(product.getId()));

		verify(productRepository, times(1)).existsById(product.getId());
		verify(productRepository, never()).deleteById(product.getId());
	}
}
