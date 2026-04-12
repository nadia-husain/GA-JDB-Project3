package com.ga.petadoption.service;

import com.ga.petadoption.exception.InformationNotFoundException;
import com.ga.petadoption.model.Pet;
import com.ga.petadoption.model.User;
import com.ga.petadoption.model.Volunteering;
import com.ga.petadoption.repository.VolunteeringRepository;
import com.ga.petadoption.security.MyUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VolunteeringService {
    private final VolunteeringRepository volunteeringRepository;

    @Autowired
    public VolunteeringService(VolunteeringRepository volunteeringRepository) {
        this.volunteeringRepository = volunteeringRepository;
    }

    public static User getCurrentLoggedInUser() {
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        return userDetails.getUser();
    }

    public Volunteering getVolunteeringById(Long volunteeringId) {
        return volunteeringRepository.findById(volunteeringId)
                .orElseThrow(() -> new InformationNotFoundException(
                        "Volunteering with ID " + volunteeringId + " does not exist."));
    }

    public Volunteering createVolunteering(Volunteering volunteeringObject) {
        volunteeringObject.setCreatedBy(getCurrentLoggedInUser());
        return volunteeringRepository.save(volunteeringObject);
    }

    public void deleteVolunteering(Long volunteeringId) {
        Volunteering volunteering = volunteeringRepository.findByIdAndUserId(volunteeringId, VolunteeringService.getCurrentLoggedInUser().getId());
        if (volunteering == null) {
            throw new InformationNotFoundException("volunteering with id " + volunteeringId + " not found");
        } else {
            volunteeringRepository.deleteById(volunteeringId);
        }
    }

    public Volunteering updateVolunteering(Long volunteeringId, Volunteering volunteeringObject) {
        Volunteering volunteering = volunteeringRepository.findByIdAndUserId(volunteeringId, VolunteeringService.getCurrentLoggedInUser().getId());
        if (volunteering == null) {
            throw new InformationNotFoundException("pet with id " + volunteeringId + " not found");
        }
        else {
            volunteering.setTask(volunteeringObject.getTask());
            return volunteeringRepository.save(volunteeringObject);
        }
    }

    public List<Volunteering> getAllVolunteering() {
        List<Volunteering> volunteering = volunteeringRepository.findAll();

        if (volunteering.isEmpty()) {
            throw new InformationNotFoundException("no volunteering was found.");
        } else {
            return volunteering;
        }
    }
}
