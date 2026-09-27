package lv.kriss.demo.teya.mapper;

import lv.kriss.demo.teya.domain.Account;
import lv.kriss.demo.teya.dto.AccountDto;

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
}
