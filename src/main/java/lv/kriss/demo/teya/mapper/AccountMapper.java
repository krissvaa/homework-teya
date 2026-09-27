package lv.kriss.demo.teya.mapper;

import lv.kriss.demo.teya.domain.Account;
import lv.kriss.demo.teya.dto.AccountDetailDto;
import lv.kriss.demo.teya.dto.AccountDto;
import lv.kriss.demo.teya.dto.BalanceDetailDto;
import lv.kriss.demo.teya.dto.CreateAccountRequest;

import java.time.Instant;
import java.util.List;

public final class AccountMapper {

    private AccountMapper() {
    }

    public static AccountDto toDto(Account account) {
        if (account == null) {
            return null;
        }
        return new AccountDto(
                account.getId(),
                account.getName(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

    public static List<AccountDto> toDtoList(List<Account> accounts) {
        return accounts.stream()
                .map(AccountMapper::toDto)
                .toList();
    }

    public static Account toEntity(CreateAccountRequest request) {
        var now = Instant.now();
        return new Account(null, request.name(), now, now, null);
    }

    public static AccountDetailDto toDetailDto(Account account, List<BalanceDetailDto> balances) {
        return new AccountDetailDto(
                account.getId(),
                account.getName(),
                balances
        );
    }
}
