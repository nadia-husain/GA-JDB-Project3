package com.ga.petadoption.service;

import com.ga.petadoption.exception.InformationExistException;
import com.ga.petadoption.exception.InformationNotFoundException;
import com.ga.petadoption.model.AdoptionRequest;
import com.ga.petadoption.model.Pet;
import com.ga.petadoption.model.User;
import com.ga.petadoption.model.enums.AdoptionRequestStatus;
import com.ga.petadoption.model.enums.PetStatus;
import com.ga.petadoption.model.enums.Role;
import com.ga.petadoption.repository.AdoptionRequestRepository;
import com.ga.petadoption.repository.PetRepository;
import com.ga.petadoption.repository.UserRepository;
import com.ga.petadoption.security.MyUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class AdoptionRequestService {

    private final AdoptionRequestRepository adoptionRequestRepository;
    private final PetRepository petRepository;
    private final UserRepository userRepository;

    // lock map per pet
    private final ConcurrentHashMap<Long, ReentrantLock> locks = new ConcurrentHashMap<>();

    @Autowired
    public AdoptionRequestService(AdoptionRequestRepository adoptionRequestRepository, PetRepository petRepository, UserRepository userRepository) {
        this.adoptionRequestRepository = adoptionRequestRepository;
        this.petRepository = petRepository;
        this.userRepository = userRepository;
    }

    private ReentrantLock getLock(Long petId) {
        return locks.computeIfAbsent(petId, id -> new ReentrantLock());
    }

    public static User getCurrentLoggedInUser() {
        return ((MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
    }

    public AdoptionRequest createAdoptionRequest(Long petId, Long userId) {
        ReentrantLock lock = getLock(petId);
        lock.lock();

        try {
            Pet pet = petRepository.findById(petId)
                    .orElseThrow(() -> new InformationNotFoundException("Pet not found"));

            if (pet.getPetStatus() != PetStatus.AVAILABLE) {
                throw new InformationExistException("Pet already adopted!");
            }

            // mark pet unavailable
            pet.setPetStatus(PetStatus.UNAVAILABLE);
            petRepository.save(pet);

            // create request
            AdoptionRequest request = new AdoptionRequest();
            request.setPet(pet);
            request.setStatus(AdoptionRequestStatus.PENDING);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new InformationNotFoundException("User not found"));

            request.setUser(user);

            return adoptionRequestRepository.save(request);

        } finally {
            lock.unlock();
        }
    }

    public List<AdoptionRequest> getAllAdoptionRequests() {
        User currentUser = getCurrentLoggedInUser();

        if (currentUser.getRole().equals(Role.ADMIN)) {
            List<AdoptionRequest> adoptionRequests = adoptionRequestRepository.findAll();
            if (adoptionRequests.isEmpty()) {
                throw new InformationNotFoundException("No requests were found.");
            }
            return adoptionRequests;
        } else {
            List<AdoptionRequest> adoptionRequests = adoptionRequestRepository.findByUserId(currentUser.getId());
            if (adoptionRequests.isEmpty()) {
                throw new InformationNotFoundException("No requests were found for your account.");
            }
            return adoptionRequests;
        }
    }

    public AdoptionRequest getAdoptionRequestById(Long adoptionRequestId) {
        return adoptionRequestRepository.findByIdAndUserId(adoptionRequestId, getCurrentLoggedInUser().getId())
                .orElseThrow(() -> new InformationNotFoundException("Adoption Request with id " + adoptionRequestId + " not found"));
    }

    public AdoptionRequest updateAdoptionRequest(Long adoptionRequestId, AdoptionRequest adoptionRequestObject) {
        AdoptionRequest adoptionRequest = adoptionRequestRepository.findById(adoptionRequestId)
                .orElseThrow(() -> new InformationNotFoundException("Adoption Request with id " + adoptionRequestId + " not found"));

        adoptionRequest.setStatus(adoptionRequestObject.getStatus());
        return adoptionRequestRepository.save(adoptionRequest);
    }

    public void deleteAdoptionRequest(Long adoptionRequestId) {
        if (!adoptionRequestRepository.existsById(adoptionRequestId)) {
            throw new InformationNotFoundException("Adoption Request with id " + adoptionRequestId + " not found");
        }
        adoptionRequestRepository.deleteById(adoptionRequestId);
    }
}