package com.ga.petadoption.service;

import com.ga.petadoption.exception.AccessDeniedException;
import com.ga.petadoption.exception.InformationExistException;
import com.ga.petadoption.exception.InformationNotFoundException;
import com.ga.petadoption.model.Pet;
import com.ga.petadoption.model.User;
import com.ga.petadoption.model.enums.PetStatus;
import com.ga.petadoption.model.enums.Role;
import com.ga.petadoption.repository.AdoptionRequestRepository;
import com.ga.petadoption.repository.PetRepository;
import com.ga.petadoption.security.MyUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Service
public class PetService {
    private final PetRepository petRepository;
    private final AdoptionRequestRepository adoptionRequestRepository;

    @Autowired
    public PetService(PetRepository petRepository, AdoptionRequestRepository adoptionRequestRepository) {
        this.petRepository = petRepository;
        this.adoptionRequestRepository = adoptionRequestRepository;
    }

    public static User getCurrentLoggedInUser() {
        return ((MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
    }

    public Pet createPet(Pet petObject, MultipartFile photo) throws IOException {
        if (!getCurrentLoggedInUser().getRole().equals(Role.ADMIN)) {
            throw new AccessDeniedException("Only admins can create pets.");
        }

        if (petRepository.existsByName(petObject.getName())) {
            throw new InformationExistException("Pet with name '" + petObject.getName() + "' already exists");
        }

        petObject.setPetStatus(PetStatus.AVAILABLE);

        if (photo != null && !photo.isEmpty()) {
            String base64Photo = Base64.getEncoder().encodeToString(photo.getBytes());
            petObject.setPhoto(base64Photo);
        }

        return petRepository.save(petObject);
    }

    public Pet getPetById(Long petId) {
        return petRepository.findById(petId)
                .orElseThrow(() -> new InformationNotFoundException("Pet with id " + petId + " not found"));
    }

    public List<Pet> getAllPets() {
        List<Pet> pets = petRepository.findAll();
        if (pets.isEmpty()) throw new InformationNotFoundException("No pets found");

        return pets;
    }

    public Pet updatePet(Long petId, Pet petObject, MultipartFile photo) throws IOException {
        if (!getCurrentLoggedInUser().getRole().equals(Role.ADMIN)) {
            throw new AccessDeniedException("Only admins can update pets.");
        }

        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new InformationNotFoundException("Pet with id " + petId + " not found"));

        pet.setName(petObject.getName());
        pet.setType(petObject.getType());
        pet.setAge(petObject.getAge());
        pet.setPetStatus(petObject.getPetStatus());

        if (photo != null && !photo.isEmpty()) {
            String base64Photo = Base64.getEncoder().encodeToString(photo.getBytes());
            pet.setPhoto(base64Photo);
        }

        return petRepository.save(pet);
    }

    public void deletePet(Long petId) {
        if (!getCurrentLoggedInUser().getRole().equals(Role.ADMIN)) {
            throw new AccessDeniedException("Only admins can delete pets.");
        }

        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new InformationNotFoundException("Pet with id " + petId + " not found"));

        if (adoptionRequestRepository.existsByPetId(petId)) {
            throw new InformationExistException("Pet with ID:" + petId + " cannot be deleted because it has an adoption request.");
        }

        petRepository.delete(pet);
    }
}
