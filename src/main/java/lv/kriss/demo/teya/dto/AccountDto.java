package lv.kriss.demo.teya.dto;

import java.time.Instant;
import java.util.UUID;

public record AccountDto(
        UUID id,
        String name,
        Instant createdAt,
        Instant updatedAt
) {
}
