package com.sidus.propert.dto;

import jakarta.validation.constraints.NotEmpty;

public record LoginRequestDTO(@NotEmpty String id,
                              @NotEmpty String password) {
}
