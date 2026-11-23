package com.aaminter.starbankrecommendation.service;

import com.aaminter.starbankrecommendation.dto.RecommendationDto;
import com.aaminter.starbankrecommendation.model.CustomerMetrics;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Applies the static eligibility rule for the Simple Credit recommendation.
 *
 * <p>The recommendation is offered when the customer has no credit product,
 * debit deposits exceed debit withdrawals, and debit withdrawals exceed
 * 100,000.</p>
 */
@Component
public class SimpleCreditRuleSet implements RecommendationRuleSet {

    private static final java.util.UUID RECOMMENDATION_ID =
            java.util.UUID.fromString("ab138afb-f3ba-4a93-b74f-0fcee86d447f");

    private static final String NAME = "Простой кредит";

    private static final String TEXT = """
            Откройте мир выгодных кредитов с нами!

            Ищете способ быстро и без лишних хлопот получить нужную сумму? Тогда наш выгодный кредит — именно то, что вам нужно! Мы предлагаем низкие процентные ставки, гибкие условия и индивидуальный подход к каждому клиенту.

            Почему выбирают нас:

            Быстрое рассмотрение заявки. Мы ценим ваше время, поэтому процесс рассмотрения заявки занимает всего несколько часов.

            Удобное оформление. Подать заявку на кредит можно онлайн на нашем сайте или в мобильном приложении.

            Широкий выбор кредитных продуктов. Мы предлагаем кредиты на различные цели: покупку недвижимости, автомобиля, образование, лечение и многое другое.

            Не упустите возможность воспользоваться выгодными условиями кредитования от нашей компании!
            """;

    /**
     * Checks whether the customer meets the Simple Credit eligibility conditions.
     *
     * @param metrics aggregated metrics of the customer
     * @return the Simple Credit recommendation when all conditions match; otherwise empty
     */
    @Override
    public Optional<RecommendationDto> check(CustomerMetrics metrics) {
        boolean depositsExceedWithdrawals =
                metrics.debitDeposits() > metrics.debitWithdrawals();

        boolean withdrawalsExceedThreshold =
                metrics.debitWithdrawals() > 100_000;

        boolean matches =
                !metrics.hasCredit()
                        && depositsExceedWithdrawals
                        && withdrawalsExceedThreshold;

        if (!matches) {
            return Optional.empty();
        }

        return Optional.of(new RecommendationDto(NAME, RECOMMENDATION_ID, TEXT));
    }
}
