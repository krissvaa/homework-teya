package lv.kriss.demo.teya.repository;

import lv.kriss.demo.teya.domain.Balance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface BalanceRepository extends JpaRepository<Balance, UUID> {

    List<Balance> findByAccountId(UUID accountId);
}