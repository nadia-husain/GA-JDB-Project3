package com.ga.petadoption.service;

import com.ga.petadoption.exception.AccessDeniedException;
import com.ga.petadoption.exception.InformationNotFoundException;
import com.ga.petadoption.model.User;
import com.ga.petadoption.model.VolunteerEvent;
import com.ga.petadoption.model.enums.Role;
import com.ga.petadoption.repository.VolunteerEventRepository;
import com.ga.petadoption.security.MyUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VolunteerEventService {
    private final VolunteerEventRepository volunteerEventRepository;

    @Autowired
    public VolunteerEventService(VolunteerEventRepository volunteerEventRepository) {
        this.volunteerEventRepository = volunteerEventRepository;
    }

    public static User getCurrentLoggedInUser() {
        return ((MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
    }

    public VolunteerEvent createVolunteerEvent(VolunteerEvent volunteerEventObject) {
        if (!getCurrentLoggedInUser().getRole().equals(Role.ADMIN)) {
            throw new AccessDeniedException("Only admins can create volunteer events.");
        }

        volunteerEventObject.setCreatedBy(getCurrentLoggedInUser());
        return volunteerEventRepository.save(volunteerEventObject);
    }

    public VolunteerEvent getVolunteerEventById(Long volunteerEventId) {
        return volunteerEventRepository.findById(volunteerEventId)
                .orElseThrow(() -> new InformationNotFoundException(
                        "volunteer event with ID " + volunteerEventId + " does not exist."));
    }

    public List<VolunteerEvent> getAllVolunteerEvents() {
        List<VolunteerEvent> volunteerEvents = volunteerEventRepository.findAll();
        if (volunteerEvents.isEmpty()) throw new InformationNotFoundException("no volunteer event was found.");
        return volunteerEvents;
    }

    public VolunteerEvent updateVolunteerEvent(Long volunteerEventId, VolunteerEvent volunteerEventObject) {
        if (!getCurrentLoggedInUser().getRole().equals(Role.ADMIN)) {
            throw new AccessDeniedException("Only admins can update volunteer events.");
        }

        VolunteerEvent volunteerEvent = volunteerEventRepository.findById(volunteerEventId)
                .orElseThrow(() -> new InformationNotFoundException("volunteer event with id " + volunteerEventId + " not found"));
        volunteerEvent.setTask(volunteerEventObject.getTask());
        return volunteerEventRepository.save(volunteerEvent);
    }

    public void deleteVolunteerEvent(Long volunteerEventId) {
        if (!getCurrentLoggedInUser().getRole().equals(Role.ADMIN)) {
            throw new AccessDeniedException("Only admins can delete volunteer events.");
        }

        volunteerEventRepository.findById(volunteerEventId)
                .orElseThrow(() -> new InformationNotFoundException("volunteer event with id " + volunteerEventId + " not found"));
        volunteerEventRepository.deleteById(volunteerEventId);
    }
}
