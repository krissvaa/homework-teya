package lv.kriss.demo.teya.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Currency;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Money {
    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;
    @Column(nullable = false, length = 3)
    private Currency currency;
}
