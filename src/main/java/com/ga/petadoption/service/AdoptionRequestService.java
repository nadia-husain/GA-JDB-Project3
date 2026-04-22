package com.ga.petadoption.service;

import com.ga.petadoption.exception.AccessDeniedException;
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
        if (getCurrentLoggedInUser().getRole() == Role.CUSTOMER) {
            if (!getCurrentLoggedInUser().getId().equals(userId)) {
                throw new AccessDeniedException("You cannot create an adoption request for others.");
            }
        }

        ReentrantLock lock = getLock(petId);
        lock.lock();

        try {
            Pet pet = petRepository.findById(petId)
                    .orElseThrow(() -> new InformationNotFoundException("Pet with id " + petId + " not found"));

            if (pet.getPetStatus() != PetStatus.AVAILABLE) {
                throw new InformationExistException("Pet with id " + petId + " is not available for adoption.");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new InformationNotFoundException("User with id " + userId + " not found"));

            pet.setPetStatus(PetStatus.UNAVAILABLE);
            petRepository.save(pet);

            AdoptionRequest request = new AdoptionRequest();
            request.setPet(pet);
            request.setUser(user);
            request.setStatus(AdoptionRequestStatus.PENDING);

            return adoptionRequestRepository.save(request);

        } finally {
            lock.unlock();
        }
    }

    public List<AdoptionRequest> getAllAdoptionRequests() {
        if (getCurrentLoggedInUser().getRole().equals(Role.ADMIN)) {
            List<AdoptionRequest> adoptionRequests = adoptionRequestRepository.findAll();
            if (adoptionRequests.isEmpty()) {
                throw new InformationNotFoundException("No requests were found.");
            }
            return adoptionRequests;
        } else {
            List<AdoptionRequest> adoptionRequests = adoptionRequestRepository.findByUserId(getCurrentLoggedInUser().getId());
            if (adoptionRequests.isEmpty()) {
                throw new InformationNotFoundException("No requests were found for your account.");
            }
            return adoptionRequests;
        }
    }

    public AdoptionRequest getAdoptionRequestById(Long adoptionRequestId) {
        if (getCurrentLoggedInUser().getRole().equals(Role.ADMIN)) {
            return adoptionRequestRepository.findById(adoptionRequestId)
                    .orElseThrow(() -> new InformationNotFoundException("Adoption Request with id " + adoptionRequestId + " not found"));
        } else {
            return adoptionRequestRepository.findByIdAndUserId(adoptionRequestId, getCurrentLoggedInUser().getId())
                    .orElseThrow(() -> new InformationNotFoundException("Adoption Request with id " + adoptionRequestId + " not found"));
        }
    }

    public AdoptionRequest updateAdoptionRequestStatus(Long adoptionRequestId, AdoptionRequestStatus status) {
        if (!getCurrentLoggedInUser().getRole().equals(Role.ADMIN)) {
            throw new AccessDeniedException("Only admins can update adoption requests.");
        }

        AdoptionRequest adoptionRequest = adoptionRequestRepository.findById(adoptionRequestId)
                .orElseThrow(() -> new InformationNotFoundException("Adoption Request with id " + adoptionRequestId + " not found"));

        adoptionRequest.setStatus(status);
        return adoptionRequestRepository.save(adoptionRequest);
    }

    public void deleteAdoptionRequest(Long adoptionRequestId) {
        if (!getCurrentLoggedInUser().getRole().equals(Role.ADMIN)) {
            throw new AccessDeniedException("Only admins can delete adoption requests.");
        }

        if (!adoptionRequestRepository.existsById(adoptionRequestId)) {
            throw new InformationNotFoundException("Adoption Request with id " + adoptionRequestId + " not found");
        }
        adoptionRequestRepository.deleteById(adoptionRequestId);
    }
}