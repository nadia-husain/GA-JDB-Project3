package com.ga.petadoption.service;

import com.ga.petadoption.exception.InformationNotFoundException;
import com.ga.petadoption.model.User;
import com.ga.petadoption.model.VolunteerEvent;
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
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        return userDetails.getUser();
    }

    public VolunteerEvent createVolunteerEvent(VolunteerEvent volunteerEventObject) {
        volunteerEventObject.setCreatedBy(getCurrentLoggedInUser());
        return volunteerEventRepository.save(volunteerEventObject);
    }

    public VolunteerEvent getVolunteerEventById(Long volunteerEventId) {
        return volunteerEventRepository.findById(volunteerEventId)
                .orElseThrow(() -> new InformationNotFoundException(
                        "volunteer vent with ID " + volunteerEventId + " does not exist."));
    }

    public List<VolunteerEvent> getAllVolunteerEvents() {
        List<VolunteerEvent> volunteerEvent = volunteerEventRepository.findAll();

        if (volunteerEvent.isEmpty()) {
            throw new InformationNotFoundException("no volunteer event was found.");
        } else {
            return volunteerEvent;
        }
    }

    public VolunteerEvent updateVolunteerEvent(Long volunteerEventId, VolunteerEvent volunteerEventObject) {
        VolunteerEvent volunteerEvent = volunteerEventRepository.findByIdAndUserId(volunteerEventId, VolunteerEventService.getCurrentLoggedInUser().getId());
        if (volunteerEvent == null) {
            throw new InformationNotFoundException("volunteer event with id " + volunteerEventId + " not found");
        }
        else {
            volunteerEvent.setTask(volunteerEventObject.getTask());
            return volunteerEventRepository.save(volunteerEventObject);
        }
    }

    public void deleteVolunteerEvent(Long volunteerEventId) {
        VolunteerEvent volunteerEvent = volunteerEventRepository.findByIdAndUserId(volunteerEventId, VolunteerEventService.getCurrentLoggedInUser().getId());
        if (volunteerEvent == null) {
            throw new InformationNotFoundException("volunteer event with id " + volunteerEventId + " not found");
        } else {
            volunteerEventRepository.deleteById(volunteerEventId);
        }
    }
}
