package com.ga.petadoption.repository;

import com.ga.petadoption.model.VolunteerEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VolunteerEventRepository extends JpaRepository<VolunteerEvent, Long> {
    Optional<VolunteerEvent> findById(Long volunteerEventId);
}
