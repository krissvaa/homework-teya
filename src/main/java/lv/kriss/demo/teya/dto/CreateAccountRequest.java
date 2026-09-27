package lv.kriss.demo.teya.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateAccountRequest(
        @NotBlank String name
) {
}
