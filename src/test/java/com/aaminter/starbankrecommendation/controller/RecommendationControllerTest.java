package com.aaminter.starbankrecommendation.controller;

import com.aaminter.starbankrecommendation.dto.RecommendationDto;
import com.aaminter.starbankrecommendation.dto.RecommendationResponse;
import com.aaminter.starbankrecommendation.service.RecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RecommendationControllerTest {

    private static final UUID USER_ID =
            UUID.fromString("cd515076-5d8a-44be-930e-8d4fcb79f42d");

    private static final UUID RECOMMENDATION_ID =
            UUID.fromString("147f6a0f-3b91-413b-ab99-87f081d60d5a");

    @Mock
    private RecommendationService recommendationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        RecommendationController controller =
                new RecommendationController(recommendationService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void shouldReturnRecommendationsAsJson() throws Exception {
        RecommendationDto recommendation = new RecommendationDto(
                "Invest 500",
                RECOMMENDATION_ID,
                "Investment description"
        );

        when(recommendationService.getRecommendations(USER_ID))
                .thenReturn(new RecommendationResponse(
                        USER_ID,
                        List.of(recommendation)
                ));

        mockMvc.perform(get("/recommendation/{user_id}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.user_id").value(USER_ID.toString()))
                .andExpect(jsonPath("$.recommendations.length()").value(1))
                .andExpect(jsonPath("$.recommendations[0].name")
                        .value("Invest 500"))
                .andExpect(jsonPath("$.recommendations[0].id")
                        .value(RECOMMENDATION_ID.toString()))
                .andExpect(jsonPath("$.recommendations[0].text")
                        .value("Investment description"));

        verify(recommendationService).getRecommendations(USER_ID);
    }

    @Test
    void shouldReturnEmptyArrayWhenNoRecommendationsExist()
            throws Exception {

        when(recommendationService.getRecommendations(USER_ID))
                .thenReturn(new RecommendationResponse(
                        USER_ID,
                        List.of()
                ));

        mockMvc.perform(get("/recommendation/{user_id}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.user_id").value(USER_ID.toString()))
                .andExpect(jsonPath("$.recommendations").isArray())
                .andExpect(jsonPath("$.recommendations.length()").value(0));

        verify(recommendationService).getRecommendations(USER_ID);
    }

    @Test
    void shouldReturnBadRequestWhenUserIdIsNotUuid() throws Exception {
        mockMvc.perform(get("/recommendation/{user_id}", "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }
}