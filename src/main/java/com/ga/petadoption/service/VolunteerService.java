package com.ga.petadoption.service;

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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class VolunteerService {
    private final VolunteerRepository volunteerRepository;
    private final UserRepository userRepository;
    private final VolunteerEventRepository volunteerEventRepository;

    private final ConcurrentHashMap<Long, ReentrantLock> locks = new ConcurrentHashMap<>();

    @Autowired
    public VolunteerService(VolunteerRepository volunteerRepository, UserRepository userRepository, VolunteerEventRepository volunteerEventRepository) {
        this.volunteerRepository = volunteerRepository;
        this.userRepository = userRepository;
        this.volunteerEventRepository = volunteerEventRepository;
    }

    private ReentrantLock getLock(Long eventId) {
        return locks.computeIfAbsent(eventId, id -> new ReentrantLock());
    }

    public static User getCurrentLoggedInUser() {
        return ((MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
    }

    @Transactional
    public Volunteer createVolunteer(Volunteer volunteer) {
        if (volunteer.getVolunteerEvent() == null || volunteer.getVolunteerEvent().getId() == null) {
            throw new InformationNotFoundException("You must provide a valid volunteerEventId in the request body!");
        }

        ReentrantLock lock = getLock(volunteer.getVolunteerEvent().getId());
        lock.lock();

        try {
            if (getCurrentLoggedInUser().getRole() == Role.CUSTOMER) {
                volunteer.setUser(getCurrentLoggedInUser());
            } else if (getCurrentLoggedInUser().getRole() == Role.ADMIN) {
                if (volunteer.getUser() == null || volunteer.getUser().getId() == null) {
                    throw new InformationNotFoundException("You must provide a valid userId in the request body!");
                }
                User targetUser = userRepository.findById(volunteer.getUser().getId())
                        .orElseThrow(() -> new InformationNotFoundException("User with id " + volunteer.getUser().getId() + " not found"));
                volunteer.setUser(targetUser);
            }

            VolunteerEvent volunteerEvent = volunteerEventRepository.findById(volunteer.getVolunteerEvent().getId())
                    .orElseThrow(() -> new InformationNotFoundException("Volunteer event with id " + volunteer.getVolunteerEvent().getId() + " not found"));

            if (volunteerEvent.getIsFull()) {
                throw new InformationExistException("Volunteer event with id " + volunteerEvent.getId() + " is full.");
            }

            volunteer.setVolunteerEvent(volunteerEvent);

            if (volunteerRepository.existsByUserIdAndVolunteerEventId(
                    volunteer.getUser().getId(), volunteer.getVolunteerEvent().getId())) {
                throw new InformationExistException("User has already volunteered for this event.");
            }

            volunteerEvent.setCapacity(volunteerEvent.getCapacity() - 1);
            if (volunteerEvent.getCapacity() == 0) {
                volunteerEvent.setIsFull(true);
            }
            volunteerEventRepository.save(volunteerEvent);

            return volunteerRepository.save(volunteer);
        } finally {
            lock.unlock();
        }
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
