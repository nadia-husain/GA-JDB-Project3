package com.ga.petadoption.service;

import com.ga.petadoption.exception.InformationNotFoundException;
import com.ga.petadoption.model.User;
import com.ga.petadoption.model.Volunteer;
import com.ga.petadoption.repository.VolunteerRepository;
import com.ga.petadoption.security.MyUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VolunteerService {
    private final VolunteerRepository volunteerRepository;

    @Autowired
    public VolunteerService(VolunteerRepository volunteerRepository) {
        this.volunteerRepository = volunteerRepository;
    }

    public static User getCurrentLoggedInUser() {
        return ((MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
    }

    public Volunteer createVolunteer(Volunteer volunteer) {
        if (volunteerRepository.existsByUserIdAndEventId(
                volunteer.getUser().getId(), volunteer.getVolunteerEvent().getId())) {
            throw new IllegalStateException("User has already volunteered for this event.");
        }
        return volunteerRepository.save(volunteer);
    }

    public Volunteer getVolunteerById(Long volunteerId) {
        return volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new InformationNotFoundException("Volunteer with id " + volunteerId + " not found"));
    }

    public List<Volunteer> getAllVolunteers() {
        List<Volunteer> volunteers = volunteerRepository.findAll();
        if (volunteers.isEmpty()) {
            throw new InformationNotFoundException("No volunteers were found.");
        }
        return volunteers;
    }

    public Volunteer updateVolunteer(Long volunteerId, Volunteer volunteerObject) {
        Volunteer volunteer = volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new InformationNotFoundException("volunteer event with id " + volunteerId + " not found"));
        volunteer.setHasAttended(volunteerObject.getHasAttended());
        return volunteerRepository.save(volunteer);
    }

    public void deleteVolunteer(Long volunteerId) {
        volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new InformationNotFoundException("volunteer with id " + volunteerId + " not found"));
        volunteerRepository.deleteById(volunteerId);
    }
}
