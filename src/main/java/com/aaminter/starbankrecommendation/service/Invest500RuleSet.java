package com.aaminter.starbankrecommendation.service;

import com.aaminter.starbankrecommendation.dto.RecommendationDto;
import com.aaminter.starbankrecommendation.model.CustomerMetrics;
import com.aaminter.starbankrecommendation.repository.RecommendationRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class Invest500RuleSet implements RecommendationRuleSet {

    private static final UUID RECOMMENDATION_ID =
            UUID.fromString("147f6a0f-3b91-413b-ab99-87f081d60d5a");

    private static final String NAME = "Invest 500";

    private static final String TEXT = """
            Откройте свой путь к успеху с индивидуальным инвестиционным счетом (ИИС) от нашего банка! Воспользуйтесь налоговыми льготами и начните инвестировать с умом. Пополните счет до конца года и получите выгоду в виде вычета на взнос в следующем налоговом периоде. Не упустите возможность разнообразить свой портфель, снизить риски и следить за актуальными рыночными тенденциями. Откройте ИИС сегодня и станьте ближе к финансовой независимости!
            """;

    private final RecommendationRepository repository;

    public Invest500RuleSet(RecommendationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<RecommendationDto> check(UUID userId) {
        CustomerMetrics metrics = repository.getCustomerMetrics(userId);

        boolean matches =
                metrics.hasDebit()
                        && !metrics.hasInvest()
                        && metrics.savingDeposits() > 1_000;

        if (!matches) {
            return Optional.empty();
        }

        return Optional.of(
                new RecommendationDto(
                        NAME,
                        RECOMMENDATION_ID,
                        TEXT
                )
        );
    }
}