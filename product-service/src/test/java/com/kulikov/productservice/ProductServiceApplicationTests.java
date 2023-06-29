package com.kulikov.productservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kulikov.productservice.dto.ProductRequest;
import com.kulikov.productservice.model.Product;
import com.kulikov.productservice.repository.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class ProductServiceApplicationTests {

	@Container
	static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:4.0.10"));

	@Autowired
	MockMvc mockMvc;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	ProductRepository productRepository;

	@DynamicPropertySource
	static void	setProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
	}

	@Test
	void shouldCreateProduct() throws Exception {
		ProductRequest productRequest = ProductRequest.builder()
				.name("1")
				.description("1")
				.price(BigDecimal.valueOf(10))
				.build();
		String prs = mapper.writeValueAsString(productRequest);

		mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
				.contentType(MediaType.APPLICATION_JSON)
				.content(prs)).andExpect(status().isCreated());
	}

	@Test
	void shouldReturnListOfProducts() throws Exception {
		Product product = Product.builder()
				.name("1")
				.build();
		Product product1 = Product.builder()
				.name("2")
				.build();
		productRepository.save(product);
		productRepository.save(product1);
		mockMvc.perform(MockMvcRequestBuilders.get("/api/product"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].name").value(product.getName()))
				.andExpect(jsonPath("$[1].name").value(product1.getName()));
		Assertions.assertEquals(2, productRepository.findAll().size());
	}
}
