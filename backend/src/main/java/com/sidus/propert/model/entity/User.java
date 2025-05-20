package com.sidus.propert.model.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.*;

@Data
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    private String id;
    private String username;
    private String password; // The digested password (not raw).
    private String role;     // "USER" , "ADMIN", etc.

    @Builder.Default
    @JsonManagedReference
    @OneToMany(mappedBy = "user", fetch=FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Project> projects = new ArrayList<>();
}