package com.ga.petadoption.repository;

import com.ga.petadoption.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<Pet, Long> {
    boolean existsByName(String name);
}