package lv.kriss.demo.teya.mapper;

import lv.kriss.demo.teya.domain.Account;
import lv.kriss.demo.teya.domain.Balance;
import lv.kriss.demo.teya.domain.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BalanceMapperTest {

    @Test
    void toDto_rescalesAmountToCurrencyDecimalPlaces_evenWhenEntityCarriesColumnScale() {
        var account = new Account(UUID.randomUUID(), "Alice", Instant.now(), Instant.now(), null);
        // Money's amount column is declared with scale = 4, so a value fetched back
        // from the database carries that scale regardless of the currency.
        var balance = balance(account, "EUR", "10.0000");

        var dto = BalanceMapper.toDto(balance);

        assertThat(dto.amount()).isEqualByComparingTo("10.00");
        assertThat(dto.amount().scale()).isEqualTo(2);
    }

    @Test
    void toDto_zeroFractionDigitCurrency_rescalesToNoDecimalPlaces() {
        var account = new Account(UUID.randomUUID(), "Alice", Instant.now(), Instant.now(), null);
        var balance = balance(account, "JPY", "500.0000");

        var dto = BalanceMapper.toDto(balance);

        assertThat(dto.amount()).isEqualByComparingTo("500");
        assertThat(dto.amount().scale()).isEqualTo(0);
    }

    @Test
    void toDetailDto_rescalesAmountToCurrencyDecimalPlaces() {
        var account = new Account(UUID.randomUUID(), "Alice", Instant.now(), Instant.now(), null);
        var balance = balance(account, "EUR", "10.0000");

        var dto = BalanceMapper.toDetailDto(balance, List.of());

        assertThat(dto.amount()).isEqualByComparingTo("10.00");
        assertThat(dto.amount().scale()).isEqualTo(2);
    }

    private static Balance balance(Account account, String currency, String amount) {
        var now = Instant.now();
        return new Balance(UUID.randomUUID(), new Money(new BigDecimal(amount), Currency.getInstance(currency)),
                account, now, now, null);
    }
}
