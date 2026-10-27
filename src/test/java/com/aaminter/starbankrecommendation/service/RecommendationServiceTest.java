package com.aaminter.starbankrecommendation.service;

import com.aaminter.starbankrecommendation.dto.RecommendationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    private static final UUID USER_ID =
            UUID.fromString("cd515076-5d8a-44be-930e-8d4fcb79f42d");

    @Mock
    private RecommendationRuleSet firstRule;

    @Mock
    private RecommendationRuleSet secondRule;

    @Mock
    private RecommendationRuleSet thirdRule;

    private RecommendationService service;

    @BeforeEach
    void setUp() {
        service = new RecommendationService(
                List.of(firstRule, secondRule, thirdRule)
        );
    }

    @Test
    void shouldCollectAllRecommendationsReturnedByRules() {
        RecommendationDto firstRecommendation = new RecommendationDto(
                "Invest 500",
                UUID.fromString("147f6a0f-3b91-413b-ab99-87f081d60d5a"),
                "Investment description"
        );

        RecommendationDto secondRecommendation = new RecommendationDto(
                "Top Saving",
                UUID.fromString("59efc529-2fff-41af-baff-90ccd7402925"),
                "Saving description"
        );

        when(firstRule.check(USER_ID))
                .thenReturn(Optional.of(firstRecommendation));
        when(secondRule.check(USER_ID))
                .thenReturn(Optional.empty());
        when(thirdRule.check(USER_ID))
                .thenReturn(Optional.of(secondRecommendation));

        var response = service.getRecommendations(USER_ID);

        assertEquals(USER_ID, response.userId());
        assertEquals(2, response.recommendations().size());
        assertEquals(
                List.of(firstRecommendation, secondRecommendation),
                response.recommendations()
        );
    }

    @Test
    void shouldReturnEmptyListWhenNoRuleMatches() {
        when(firstRule.check(USER_ID)).thenReturn(Optional.empty());
        when(secondRule.check(USER_ID)).thenReturn(Optional.empty());
        when(thirdRule.check(USER_ID)).thenReturn(Optional.empty());

        var response = service.getRecommendations(USER_ID);

        assertEquals(USER_ID, response.userId());
        assertEquals(0, response.recommendations().size());
    }
}