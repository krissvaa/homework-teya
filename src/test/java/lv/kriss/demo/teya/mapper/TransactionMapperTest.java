package lv.kriss.demo.teya.mapper;

import lv.kriss.demo.teya.domain.Balance;
import lv.kriss.demo.teya.domain.Money;
import lv.kriss.demo.teya.domain.Transaction;
import lv.kriss.demo.teya.domain.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionMapperTest {

    @Test
    void toDto_rescalesAmountAndBalanceAfterToCurrencyDecimalPlaces_evenWhenEntityCarriesColumnScale() {
        // Money's amount column and balanceAfter column are both declared with scale = 4,
        // so values fetched back from the database carry that scale regardless of the currency.
        var transaction = transaction("EUR", "10.0000", "110.0000");

        var dto = TransactionMapper.toDto(transaction);

        assertThat(dto.amount()).isEqualByComparingTo("10.00");
        assertThat(dto.amount().scale()).isEqualTo(2);
        assertThat(dto.balanceAfter()).isEqualByComparingTo("110.00");
        assertThat(dto.balanceAfter().scale()).isEqualTo(2);
    }

    @Test
    void toDto_zeroFractionDigitCurrency_rescalesToNoDecimalPlaces() {
        var transaction = transaction("JPY", "500.0000", "1500.0000");

        var dto = TransactionMapper.toDto(transaction);

        assertThat(dto.amount()).isEqualByComparingTo("500");
        assertThat(dto.amount().scale()).isEqualTo(0);
        assertThat(dto.balanceAfter()).isEqualByComparingTo("1500");
        assertThat(dto.balanceAfter().scale()).isEqualTo(0);
    }

    private static Transaction transaction(String currency, String amount, String balanceAfter) {
        var currencyInstance = Currency.getInstance(currency);
        var now = Instant.now();
        var balance = new Balance(UUID.randomUUID(), new Money(new BigDecimal(balanceAfter), currencyInstance),
                null, now, now, null);
        return new Transaction(UUID.randomUUID(), balance, new Money(new BigDecimal(amount), currencyInstance),
                TransactionType.DEPOSIT, new BigDecimal(balanceAfter), now);
    }
}
