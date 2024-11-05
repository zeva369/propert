package com.seva.propert.model.pert;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class Node {
    private Long id;
    private String label;
    
    @JsonIgnore
    private Map<String,Edge> previous = new HashMap<>();

    @JsonIgnore
    private Map<String,Edge> next = new HashMap<>(); 
    
    //This attributes are specific of PERT's critial path calculation
    //Maybe there would be needed a State class for containing this, like Task contains es, ef, ls & lf
    private Double start = 0d;
    private Double end = 0d;
    private Boolean critical = false;
    
    public Node(Long id, String label) {
        this.id = id;
        this.label = label;
    }

    public Boolean isInitialNode() {
        return previous.isEmpty();
    }

    public Boolean isFinalNode() {
        return next.isEmpty();
    }

}
