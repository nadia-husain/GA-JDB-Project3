package com.ga.petadoption.repository;

import com.ga.petadoption.model.AdoptionRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdoptionRequestRepository extends JpaRepository<AdoptionRequest, Long> {
    List<AdoptionRequest> findByUserId(Long userId);
    Optional<AdoptionRequest> findById(Long adoptionRequestId);
    Optional<AdoptionRequest> findByIdAndUserId(Long adoptionRequestId, Long userId);
}
