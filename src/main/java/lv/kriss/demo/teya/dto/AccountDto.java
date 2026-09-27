package lv.kriss.demo.teya.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.UUID;

public record AccountDto(
        UUID id,
        @NotBlank String name,
        Instant createdAt,
        Instant updatedAt
) {
}
