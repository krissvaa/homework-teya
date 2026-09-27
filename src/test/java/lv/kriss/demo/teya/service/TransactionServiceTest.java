package lv.kriss.demo.teya.service;

import lv.kriss.demo.teya.domain.Balance;
import lv.kriss.demo.teya.domain.Money;
import lv.kriss.demo.teya.domain.Transaction;
import lv.kriss.demo.teya.domain.TransactionType;
import lv.kriss.demo.teya.dto.CreateTransactionRequest;
import lv.kriss.demo.teya.exception.InvalidTransactionException;
import lv.kriss.demo.teya.exception.ResourceNotFoundException;
import lv.kriss.demo.teya.repository.BalanceRepository;
import lv.kriss.demo.teya.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TransactionServiceTest {

    private final TransactionRepository transactionRepository = mock(TransactionRepository.class);
    private final BalanceRepository balanceRepository = mock(BalanceRepository.class);
    private final TransactionService service = new TransactionService(transactionRepository, balanceRepository);

    @Test
    void deposit_increasesBalanceAndPersistsBoth() {
        var balanceId = UUID.randomUUID();
        var balance = balance(balanceId, "EUR", "100.00");
        when(balanceRepository.findById(balanceId)).thenReturn(Optional.of(balance));
        when(balanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var request = new CreateTransactionRequest(balanceId, new BigDecimal("50.00"), "EUR", TransactionType.DEPOSIT);
        var result = service.createTransaction(request);

        assertThat(result.balanceId()).isEqualTo(balanceId);
        assertThat(result.amount()).isEqualByComparingTo("50.00");
        assertThat(result.currency()).isEqualTo("EUR");
        assertThat(result.type()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(result.balanceAfter()).isEqualByComparingTo("150.00");
        assertThat(balance.getBalance().getAmount()).isEqualByComparingTo("150.00");
        verify(balanceRepository).save(balance);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void withdrawal_decreasesBalanceAndPersistsBoth() {
        var balanceId = UUID.randomUUID();
        var balance = balance(balanceId, "EUR", "100.00");
        when(balanceRepository.findById(balanceId)).thenReturn(Optional.of(balance));
        when(balanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var request = new CreateTransactionRequest(balanceId, new BigDecimal("40.00"), "EUR", TransactionType.WITHDRAWAL);
        var result = service.createTransaction(request);

        assertThat(result.type()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(result.balanceAfter()).isEqualByComparingTo("60.00");
        assertThat(balance.getBalance().getAmount()).isEqualByComparingTo("60.00");
    }

    @Test
    void withdrawal_drainingBalanceToExactlyZero_isAllowed() {
        var balanceId = UUID.randomUUID();
        var balance = balance(balanceId, "EUR", "100.00");
        when(balanceRepository.findById(balanceId)).thenReturn(Optional.of(balance));
        when(balanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var request = new CreateTransactionRequest(balanceId, new BigDecimal("100.00"), "EUR", TransactionType.WITHDRAWAL);
        var result = service.createTransaction(request);

        assertThat(result.balanceAfter()).isEqualByComparingTo("0.00");
        verify(balanceRepository).save(balance);
    }

    @Test
    void withdrawal_insufficientFunds_throwsAndDoesNotPersistOrMutateBalance() {
        var balanceId = UUID.randomUUID();
        var balance = balance(balanceId, "EUR", "100.00");
        when(balanceRepository.findById(balanceId)).thenReturn(Optional.of(balance));

        var request = new CreateTransactionRequest(balanceId, new BigDecimal("150.00"), "EUR", TransactionType.WITHDRAWAL);

        assertThatThrownBy(() -> service.createTransaction(request))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("Insufficient funds")
                .hasMessageContaining(balanceId.toString());

        assertThat(balance.getBalance().getAmount()).isEqualByComparingTo("100.00");
        verify(balanceRepository, never()).save(any());
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void currencyMismatch_throwsAndDoesNotPersistOrMutateBalance() {
        var balanceId = UUID.randomUUID();
        var balance = balance(balanceId, "EUR", "100.00");
        when(balanceRepository.findById(balanceId)).thenReturn(Optional.of(balance));

        var request = new CreateTransactionRequest(balanceId, new BigDecimal("10.00"), "USD", TransactionType.DEPOSIT);

        assertThatThrownBy(() -> service.createTransaction(request))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("Currency mismatch")
                .hasMessageContaining("EUR")
                .hasMessageContaining("USD");

        assertThat(balance.getBalance().getAmount()).isEqualByComparingTo("100.00");
        verify(balanceRepository, never()).save(any());
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void deposit_amountWithFewerDecimalPlacesThanCurrency_isNormalizedToCurrencyScale() {
        var balanceId = UUID.randomUUID();
        var balance = balance(balanceId, "EUR", "100.00");
        when(balanceRepository.findById(balanceId)).thenReturn(Optional.of(balance));
        when(balanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var request = new CreateTransactionRequest(balanceId, new BigDecimal("10"), "EUR", TransactionType.DEPOSIT);
        service.createTransaction(request);

        var transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        assertThat(transactionCaptor.getValue().getAmount().getAmount().scale()).isEqualTo(2);
        assertThat(balance.getBalance().getAmount().scale()).isEqualTo(2);
    }

    @Test
    void deposit_tooManyDecimalDigitsForCurrency_throwsAndDoesNotPersistOrMutateBalance() {
        var balanceId = UUID.randomUUID();
        var balance = balance(balanceId, "EUR", "100.00");
        when(balanceRepository.findById(balanceId)).thenReturn(Optional.of(balance));

        var request = new CreateTransactionRequest(balanceId, new BigDecimal("10.001"), "EUR", TransactionType.DEPOSIT);

        assertThatThrownBy(() -> service.createTransaction(request))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("decimal digits")
                .hasMessageContaining("EUR");

        assertThat(balance.getBalance().getAmount()).isEqualByComparingTo("100.00");
        verify(balanceRepository, never()).save(any());
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void withdrawal_tooManyDecimalDigitsForZeroFractionCurrency_throws() {
        var balanceId = UUID.randomUUID();
        var balance = balance(balanceId, "JPY", "100");
        when(balanceRepository.findById(balanceId)).thenReturn(Optional.of(balance));

        var request = new CreateTransactionRequest(balanceId, new BigDecimal("10.5"), "JPY", TransactionType.WITHDRAWAL);

        assertThatThrownBy(() -> service.createTransaction(request))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("decimal digits")
                .hasMessageContaining("JPY");

        assertThat(balance.getBalance().getAmount()).isEqualByComparingTo("100");
        verify(balanceRepository, never()).save(any());
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void deposit_amountWithTrailingZerosWithinFractionDigits_isAllowed() {
        var balanceId = UUID.randomUUID();
        var balance = balance(balanceId, "EUR", "100.00");
        when(balanceRepository.findById(balanceId)).thenReturn(Optional.of(balance));
        when(balanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var request = new CreateTransactionRequest(balanceId, new BigDecimal("10.500"), "EUR", TransactionType.DEPOSIT);
        var result = service.createTransaction(request);

        assertThat(result.balanceAfter()).isEqualByComparingTo("110.50");
    }

    @Test
    void unknownBalance_throwsResourceNotFoundAndDoesNotTouchTransactionRepository() {
        var balanceId = UUID.randomUUID();
        when(balanceRepository.findById(balanceId)).thenReturn(Optional.empty());

        var request = new CreateTransactionRequest(balanceId, new BigDecimal("10.00"), "EUR", TransactionType.DEPOSIT);

        assertThatThrownBy(() -> service.createTransaction(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(balanceId.toString());

        verify(balanceRepository, never()).save(any());
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void deposit_refreshesBalanceUpdatedAtTimestamp() {
        var balanceId = UUID.randomUUID();
        var balance = balance(balanceId, "EUR", "100.00");
        var originalUpdatedAt = Instant.now().minus(1, ChronoUnit.DAYS);
        balance.setUpdatedAt(originalUpdatedAt);
        when(balanceRepository.findById(balanceId)).thenReturn(Optional.of(balance));
        when(balanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var request = new CreateTransactionRequest(balanceId, new BigDecimal("10.00"), "EUR", TransactionType.DEPOSIT);
        service.createTransaction(request);

        assertThat(balance.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    private static Balance balance(UUID id, String currency, String amount) {
        var now = Instant.now();
        return new Balance(id, new Money(new BigDecimal(amount), Currency.getInstance(currency)), null, now, now, null);
    }
}
