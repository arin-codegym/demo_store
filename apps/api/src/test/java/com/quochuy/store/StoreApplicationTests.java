package com.quochuy.store;

import com.quochuy.ai.embedding.EmbeddingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StoreApplicationTests {
	@Autowired
	private EmbeddingService embeddingService;
	@Autowired
	private org.springframework.core.env.Environment env;
	
	@Test
	void env() {
		System.out.println("genai api-key = " + env.getProperty("spring.ai.google.genai.api-key"));
		System.out.println("embedding api-key = " + env.getProperty("spring.ai.google.genai.embedding.api-key"));
		System.out.println("embedding model = " + env.getProperty("spring.ai.google.genai.embedding.text.options.model"));
	}
//	@Test
//	void contextLoads() {
//		List<Float> v = embeddingService.embed("xin chao");
//
//		System.out.println("embedding size = " + v.size());
//		System.out.println(v);
//
//		assertNotNull(v);
//		assertFalse(v.isEmpty());
//		assertEquals(1536, v.size());
//	}

}
