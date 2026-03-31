package com.ga.petadoption.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ga.petadoption.model.enums.Role;
import com.ga.petadoption.model.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"password", "userProfile"})
public class User {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String emailAddress;

    @Column
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column
    private Boolean verified;

    @Column
    @Enumerated(EnumType.STRING)
    private UserStatus userStatus;

//    @JsonIgnore
//    @OneToMany(fetch = FetchType.LAZY, mappedBy = "customer", orphanRemoval = true)
//    private List<AdoptionRequests> adoptionRequests;
//
//    @JsonIgnore
//    @OneToMany(fetch = FetchType.LAZY, mappedBy = "owner", orphanRemoval = true)
//    private List<Pet> pet;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "profile_id", referencedColumnName = "id")
    private UserProfile userProfile;

    @JsonIgnore
    public String getPassword() {
        return password;
    }
}
