package com.sidus.propert.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record TaskDTO (
        @NotEmpty UUID id,
        @NotEmpty String label,
        @NotEmpty String description,
        @NotNull Double length,
        @NotNull Long projectId,
        @NotNull List<String> predecessors ){

}
