package lv.kriss.demo.teya.repository;

import lv.kriss.demo.teya.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByBalanceId(UUID balanceId);
}