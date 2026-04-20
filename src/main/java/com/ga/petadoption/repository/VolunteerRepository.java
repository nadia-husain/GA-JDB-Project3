package com.ga.petadoption.repository;

import com.ga.petadoption.model.Volunteer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {
    boolean existsByUserIdAndVolunteerEventId(Long userId, Long volunteerEventId);
}
