package com.aaminter.starbankrecommendation.service;

import com.aaminter.starbankrecommendation.dto.RecommendationDto;
import com.aaminter.starbankrecommendation.model.CustomerMetrics;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TopSavingRuleSet implements RecommendationRuleSet {

    private static final java.util.UUID RECOMMENDATION_ID =
            java.util.UUID.fromString("59efc529-2fff-41af-baff-90ccd7402925");

    private static final String NAME = "Top Saving";

    private static final String TEXT = """
            Откройте свою собственную «Копилку» с нашим банком! «Копилка» — это уникальный банковский инструмент, который поможет вам легко и удобно накапливать деньги на важные цели. Больше никаких забытых чеков и потерянных квитанций — всё под контролем!

            Преимущества «Копилки»:

            Накопление средств на конкретные цели. Установите лимит и срок накопления, и банк будет автоматически переводить определенную сумму на ваш счет.

            Прозрачность и контроль. Отслеживайте свои доходы и расходы, контролируйте процесс накопления и корректируйте стратегию при необходимости.

            Безопасность и надежность. Ваши средства находятся под защитой банка, а доступ к ним возможен только через мобильное приложение или интернет-банкинг.

            Начните использовать «Копилку» уже сегодня и станьте ближе к своим финансовым целям!
            """;

    @Override
    public Optional<RecommendationDto> check(CustomerMetrics metrics) {
        boolean hasEnoughDeposits =
                metrics.debitDeposits() >= 50_000
                        || metrics.savingDeposits() >= 50_000;

        boolean depositsExceedWithdrawals =
                metrics.debitDeposits() > metrics.debitWithdrawals();

        boolean matches =
                metrics.hasDebit()
                        && hasEnoughDeposits
                        && depositsExceedWithdrawals;

        if (!matches) {
            return Optional.empty();
        }

        return Optional.of(new RecommendationDto(NAME, RECOMMENDATION_ID, TEXT));
    }
}
