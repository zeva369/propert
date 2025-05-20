package com.sidus.propert.dto;

import jakarta.validation.constraints.NotEmpty;

public record UserInDTO(@NotEmpty String id,
                        @NotEmpty String username,
                        @NotEmpty String password) {

}
