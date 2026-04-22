package com.ga.petadoption.service;

import com.ga.petadoption.exception.AccessDeniedException;
import com.ga.petadoption.exception.InformationExistException;
import com.ga.petadoption.exception.InformationNotFoundException;
import com.ga.petadoption.model.User;
import com.ga.petadoption.model.Volunteer;
import com.ga.petadoption.model.VolunteerEvent;
import com.ga.petadoption.model.enums.Role;
import com.ga.petadoption.repository.UserRepository;
import com.ga.petadoption.repository.VolunteerEventRepository;
import com.ga.petadoption.repository.VolunteerRepository;
import com.ga.petadoption.security.MyUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VolunteerService {
    private final VolunteerRepository volunteerRepository;
    private final UserRepository userRepository;
    private final VolunteerEventRepository volunteerEventRepository;

    @Autowired
    public VolunteerService(VolunteerRepository volunteerRepository, UserRepository userRepository, VolunteerEventRepository volunteerEventRepository) {
        this.volunteerRepository = volunteerRepository;
        this.userRepository = userRepository;
        this.volunteerEventRepository = volunteerEventRepository;
    }

    public static User getCurrentLoggedInUser() {
        return ((MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
    }

    @Transactional
    public Volunteer createVolunteer(Volunteer volunteer) {
        if (volunteer.getVolunteerEvent() == null || volunteer.getVolunteerEvent().getId() == null) {
            throw new InformationNotFoundException("You must provide a valid volunteerEventId!");
        }

        VolunteerEvent volunteerEvent = volunteerEventRepository
                .findById(volunteer.getVolunteerEvent().getId())
                .orElseThrow(() -> new InformationNotFoundException("Event not found"));

        if (volunteerEvent.getIsFull()) {
            throw new InformationExistException("Event is full.");
        }

        if (volunteerRepository.existsByUserIdAndVolunteerEventId(
                volunteer.getUser().getId(), volunteerEvent.getId())) {
            throw new InformationExistException("User already volunteered for this event.");
        }

        volunteerEvent.setCapacity(volunteerEvent.getCapacity() - 1);
        if (volunteerEvent.getCapacity() == 0) {
            volunteerEvent.setIsFull(true);
        }
        volunteerEventRepository.save(volunteerEvent);

        return volunteerRepository.save(volunteer);
        // If two threads modified the same event, JPA throws OptimisticLockException here
    }

    public Volunteer getVolunteerById(Long volunteerId) {
        if (!getCurrentLoggedInUser().getRole().equals(Role.ADMIN)) {
            throw new AccessDeniedException("Only admins can view volunteer.");
        }

        return volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new InformationNotFoundException("Volunteer with id " + volunteerId + " not found"));
    }

    public List<Volunteer> getAllVolunteers() {
        if (!getCurrentLoggedInUser().getRole().equals(Role.ADMIN)) {
            throw new AccessDeniedException("Only admins can view volunteers.");
        }

        List<Volunteer> volunteers = volunteerRepository.findAll();
        if (volunteers.isEmpty()) {
            throw new InformationNotFoundException("No volunteers were found.");
        }
        return volunteers;
    }

    public Volunteer updateVolunteer(Long volunteerId, Volunteer volunteerObject) {
        if (!getCurrentLoggedInUser().getRole().equals(Role.ADMIN)) {
            throw new AccessDeniedException("Only admins can update volunteer.");
        }

        Volunteer volunteer = volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new InformationNotFoundException("volunteer event with id " + volunteerId + " not found"));
        volunteer.setHasAttended(volunteerObject.getHasAttended());
        return volunteerRepository.save(volunteer);
    }

    public void deleteVolunteer(Long volunteerId) {
        if (!getCurrentLoggedInUser().getRole().equals(Role.ADMIN)) {
            throw new AccessDeniedException("Only admins can delete volunteer.");
        }

        volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new InformationNotFoundException("volunteer with id " + volunteerId + " not found"));
        volunteerRepository.deleteById(volunteerId);
    }
}
