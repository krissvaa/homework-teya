package lv.kriss.demo.teya.repository;

import lv.kriss.demo.teya.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

}
