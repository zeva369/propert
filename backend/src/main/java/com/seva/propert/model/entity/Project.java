package com.seva.propert.model.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PostLoad;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Project {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "{NotBlank.project.name}")
	@Column(nullable = false)
    private String name;
    private String description;
    
    @JsonManagedReference
    @OneToMany(mappedBy = "project", fetch=FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Task> tasks = new ArrayList<>();

    //Different from tasks as tasks contains only the ones created by the user and
    //workflow manage those plus the dummy tasks auto-generated
    // @Transient
    // private Workflow workflow = null;

    @PostLoad
    public void initialize() {
        //Initialize workflow data
        //workflow = new Workflow(tasks);
    }

    
}
