package lv.kriss.demo.teya.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

@Embeddable
public class Money {
    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;
    @Column(nullable = false, length = 3)
    private Currency currency;
}
