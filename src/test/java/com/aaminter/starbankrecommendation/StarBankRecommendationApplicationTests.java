
package com.aaminter.starbankrecommendation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:recommendation_integration;DB_CLOSE_DELAY=-1",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.datasource.hikari.read-only=false"
})
@AutoConfigureMockMvc
class StarBankRecommendationApplicationTests {

	private static final UUID INVEST_500_ID =
			UUID.fromString("147f6a0f-3b91-413b-ab99-87f081d60d5a");

	private static final UUID USER_ID =
			UUID.fromString("cd515076-5d8a-44be-930e-8d4fcb79f42d");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private UUID debitProductId;
	private UUID savingProductId;

	@BeforeEach
	void setUp() {
		jdbcTemplate.execute("DROP TABLE IF EXISTS TRANSACTIONS");
		jdbcTemplate.execute("DROP TABLE IF EXISTS PRODUCTS");

		jdbcTemplate.execute("""
                CREATE TABLE PRODUCTS (
                    ID UUID PRIMARY KEY,
                    TYPE VARCHAR(255) NOT NULL,
                    NAME VARCHAR(255) NOT NULL
                )
                """);

		jdbcTemplate.execute("""
                CREATE TABLE TRANSACTIONS (
                    ID UUID PRIMARY KEY,
                    PRODUCT_ID UUID NOT NULL,
                    USER_ID UUID NOT NULL,
                    TYPE VARCHAR(255) NOT NULL,
                    AMOUNT INTEGER NOT NULL
                )
                """);

		debitProductId = insertProduct("DEBIT", "Debit");
		savingProductId = insertProduct("SAVING", "Saving");
	}

	@Test
	void shouldReturnInvest500RecommendationThroughHttpApi()
			throws Exception {

		insertTransaction(debitProductId, USER_ID, "DEPOSIT", 1_000);
		insertTransaction(savingProductId, USER_ID, "DEPOSIT", 1_200);

		mockMvc.perform(get("/recommendation/{user_id}", USER_ID))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
				.andExpect(jsonPath("$.user_id").value(USER_ID.toString()))
				.andExpect(jsonPath("$.recommendations.length()").value(1))
				.andExpect(jsonPath("$.recommendations[0].name")
						.value("Invest 500"))
				.andExpect(jsonPath("$.recommendations[0].id")
						.value(INVEST_500_ID.toString()))
				.andExpect(jsonPath("$.recommendations[0].text").isNotEmpty());
	}

	@Test
	void shouldReturnEmptyRecommendationsThroughHttpApi()
			throws Exception {

		mockMvc.perform(get(
						"/recommendation/{user_id}",
						USER_ID
				))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
				.andExpect(jsonPath("$.user_id").value(USER_ID.toString()))
				.andExpect(jsonPath("$.recommendations").isArray())
				.andExpect(jsonPath("$.recommendations.length()").value(0));
	}

	private UUID insertProduct(String type, String name) {
		UUID productId = UUID.randomUUID();

		jdbcTemplate.update(
				"INSERT INTO PRODUCTS (ID, TYPE, NAME) VALUES (?, ?, ?)",
				productId,
				type,
				name
		);

		return productId;
	}

	private void insertTransaction(
			UUID productId,
			UUID userId,
			String type,
			int amount
	) {
		jdbcTemplate.update(
				"""
                INSERT INTO TRANSACTIONS (ID, PRODUCT_ID, USER_ID, TYPE, AMOUNT)
                VALUES (?, ?, ?, ?, ?)
                """,
				UUID.randomUUID(),
				productId,
				userId,
				type,
				amount
		);
	}
}
