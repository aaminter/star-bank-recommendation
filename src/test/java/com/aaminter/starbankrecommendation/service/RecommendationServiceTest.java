package com.aaminter.starbankrecommendation.service;

import com.aaminter.starbankrecommendation.dto.RecommendationDto;
import com.aaminter.starbankrecommendation.model.CustomerMetrics;
import com.aaminter.starbankrecommendation.repository.RecommendationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    private static final UUID USER_ID =
            UUID.fromString("cd515076-5d8a-44be-930e-8d4fcb79f42d");

    private static final CustomerMetrics METRICS =
            new CustomerMetrics(true, false, false, 10_000, 60_000, 20_000);

    @Mock
    private RecommendationRepository repository;

    @Mock
    private RecommendationRuleSet firstRule;

    @Mock
    private RecommendationRuleSet secondRule;

    @Mock
    private RecommendationRuleSet thirdRule;

    private RecommendationService service;

    @BeforeEach
    void setUp() {
        service = new RecommendationService(repository, List.of(firstRule, secondRule, thirdRule));
    }

    @Test
    void shouldCollectRecommendationsAndLoadMetricsOnlyOnce() {
        RecommendationDto first = new RecommendationDto(
                "Invest 500",
                UUID.fromString("147f6a0f-3b91-413b-ab99-87f081d60d5a"),
                "Investment description"
        );
        RecommendationDto second = new RecommendationDto(
                "Top Saving",
                UUID.fromString("59efc529-2fff-41af-baff-90ccd7402925"),
                "Saving description"
        );

        when(repository.getCustomerMetrics(USER_ID)).thenReturn(METRICS);
        when(firstRule.check(METRICS)).thenReturn(Optional.of(first));
        when(secondRule.check(METRICS)).thenReturn(Optional.empty());
        when(thirdRule.check(METRICS)).thenReturn(Optional.of(second));

        var response = service.getRecommendations(USER_ID);

        assertEquals(USER_ID, response.userId());
        assertEquals(List.of(first, second), response.recommendations());
        verify(repository).getCustomerMetrics(USER_ID);
        verify(firstRule).check(METRICS);
        verify(secondRule).check(METRICS);
        verify(thirdRule).check(METRICS);
    }

    @Test
    void shouldReturnEmptyListWhenNoRuleMatches() {
        when(repository.getCustomerMetrics(USER_ID)).thenReturn(METRICS);
        when(firstRule.check(METRICS)).thenReturn(Optional.empty());
        when(secondRule.check(METRICS)).thenReturn(Optional.empty());
        when(thirdRule.check(METRICS)).thenReturn(Optional.empty());

        var response = service.getRecommendations(USER_ID);

        assertEquals(USER_ID, response.userId());
        assertEquals(0, response.recommendations().size());
        verify(repository).getCustomerMetrics(USER_ID);
    }
}
