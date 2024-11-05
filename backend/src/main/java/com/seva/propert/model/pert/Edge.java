package com.seva.propert.model.pert;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Edge {
    private String id;
    private String label;
    @JsonBackReference
    private Node from = null;
    @JsonBackReference
    private Node to = null; 
    private Boolean critical = false;
}
