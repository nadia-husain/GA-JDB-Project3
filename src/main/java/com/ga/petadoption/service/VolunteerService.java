package com.ga.petadoption.service;

import com.ga.petadoption.exception.InformationNotFoundException;
import com.ga.petadoption.model.Pet;
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
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        return userDetails.getUser();
    }

    public Volunteer getVolunteerById(Long volunteerId) {
        Volunteer volunteer = volunteerRepository.findByIdAndUserId(volunteerId, VolunteerService.getCurrentLoggedInUser().getId());
        if (volunteer == null) {
            throw new InformationNotFoundException("category with id " + volunteerId + " not found");
        } else {
            return volunteer;
        }
    }

    public List<Volunteer> getAllVolunteers() {
        List<Volunteer> volunteers = volunteerRepository.findAll();

        if (volunteers.isEmpty()) {
            throw new InformationNotFoundException("no volunteers were found.");
        } else {
            return volunteers;
        }
    }

    public void deleteVolunteer(Long volunteerId) {
        Volunteer volunteer = volunteerRepository.findByIdAndUserId(volunteerId, VolunteerService.getCurrentLoggedInUser().getId());
        if (volunteer == null) {
            throw new InformationNotFoundException("volunteer with id " + volunteerId + " not found");
        } else {
            volunteerRepository.deleteById(volunteerId);
        }
    }
}
