package com.sidus.propert.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Data Transfer Object for Task input.
 * This class is used to transfer data from the client to the server.
 * It contains the fields that are required to create or update a Task.
 *
 * This data transfer object dows not include the projectId field,
 * because it is obtained from the endpoint URL.
 */
public record TaskInDTO(
        @NotEmpty String id,
        @NotEmpty String description,
        @NotNull Double length,
        @NotNull List<String> predecessors ){

}
