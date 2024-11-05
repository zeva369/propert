package com.seva.propert.model.pert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Edge {
    private String id;
    private String label;

    @JsonIgnore
    private Node from = null;

    @JsonIgnore
    private Node to = null; 
    private Boolean critical = false;

    @JsonProperty("from")
    public Long getFromId(){
        return from.getId();
    }

    @JsonProperty("to")
    public Long getToId(){
        return to.getId();
    }
}
