package com.sidus.propert.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
public class TaskInDTO {
    private String id;
    private String description;
    private Double length;

    private List<String> predecessors = new ArrayList<>();

}
