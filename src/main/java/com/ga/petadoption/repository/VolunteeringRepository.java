package com.ga.petadoption.repository;

import com.ga.petadoption.model.Pet;
import com.ga.petadoption.model.Volunteering;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VolunteeringRepository extends JpaRepository<Volunteering, Long> {
    Optional<Volunteering> findById(Long volunteeringId);
    Volunteering findByTask(String task);
    Volunteering findByIdAndUserId(Long volunteeringId, Long userId);
}
