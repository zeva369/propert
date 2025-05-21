package com.sidus.propert.dto;

import java.util.List;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record TaskDTO (
        @NotEmpty String id,
        @NotEmpty String description,
        @NotNull Double length,
        @NotNull Long projectId,
        @NotNull List<String> predecessors ){

}
