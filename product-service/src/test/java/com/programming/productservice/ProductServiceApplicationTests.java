package com.programming.productservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.programming.productservice.dto.ProductRequest;
import com.programming.productservice.dto.ProductResponse;
import com.programming.productservice.repository.ProductRepository;
import com.programming.productservice.service.ProductService;
import org.junit.jupiter.api.AfterEach;
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

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.web.servlet.function.ServerResponse.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class ProductServiceApplicationTests {

	@Container
	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ProductService productService;
	@Autowired
	private ProductRepository productRepository;

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry){
		dynamicPropertyRegistry.add("spring.data.mongodb.uri",mongoDBContainer::getReplicaSetUrl);
	}

	@AfterEach
	public void tearDown(){
		productRepository.deleteAll();
	}

	@Test
	void createProductTest() throws Exception {

		ProductRequest productRequest = getProductRequest();

		String productRequestString = objectMapper.writeValueAsString(productRequest);

		mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
						.contentType(MediaType.APPLICATION_JSON).
						content(productRequestString))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated());

		Assertions.assertEquals(1, productRepository.findAll().size());

	}

	@Test
	void getProductEmptyTest() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/api/product"))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
	}

	@Test
	void getProductTest() throws Exception {

		ProductRequest productRequest = getProductRequest();

		String productRequestString = objectMapper.writeValueAsString(productRequest);

		// save a Product via Post Request
		mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
						.contentType(MediaType.APPLICATION_JSON).
						content(productRequestString))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated());

		// create expected Response
		List< ProductResponse > productResponses = getProductResponse();

		String productResponsesString = objectMapper.writeValueAsString(productResponses);


		// send Get Request
		mockMvc.perform(MockMvcRequestBuilders.get("/api/product"))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
				.andExpect(content().json(productResponsesString));
	}

	private List<ProductResponse> getProductResponse() {
		return productService.getAllProducts();
	}

	private ProductRequest getProductRequest() {

		return ProductRequest.builder()
				.description("Future Iphone")
				.price(BigDecimal.valueOf(200))
				.name("Iphone 20")
				.build();

	}

}
