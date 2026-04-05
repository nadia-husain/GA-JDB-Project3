package com.ga.petadoption.repository;

import com.ga.petadoption.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, Long> {
    List<Pet> findByUserId(Long userId);

    Pet findByName(String name);

    Pet findByIdAndUserId(Long petId, Long userId);

    Pet findByUserIdAndName(Long userId, String name);
}