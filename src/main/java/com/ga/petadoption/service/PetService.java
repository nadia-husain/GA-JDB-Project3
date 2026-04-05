package com.ga.petadoption.service;

import com.ga.petadoption.exception.InformationExistException;
import com.ga.petadoption.exception.InformationNotFoundException;
import com.ga.petadoption.model.Pet;
import com.ga.petadoption.model.User;
import com.ga.petadoption.repository.PetRepository;
import com.ga.petadoption.security.MyUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PetService {
    private final PetRepository petRepository;

    @Autowired
    public PetService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    public static User getCurrentLoggedInUser() {
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        return userDetails.getUser();
    }

    public Pet createPet(Pet petObject) {
        Pet pet = petRepository.findByName(petObject.getName());

        if (pet != null) {
            throw new InformationExistException("pet with name " + pet.getName() + " already exists! Choose another name");
        } else {
            petObject.setUser(getCurrentLoggedInUser());
            return petRepository.save(petObject);
        }
    }

    public Pet getPetById(Long petId) {
        Pet pet = petRepository.findByIdAndUserId(petId, PetService.getCurrentLoggedInUser().getId());
        if (pet == null) {
            throw new InformationNotFoundException("category with id " + petId + " not found");
        } else {
            return pet;
        }
    }

    public Pet updatePet(Long petId, Pet petObject) {
        Pet pet = petRepository.findByIdAndUserId(petId, PetService.getCurrentLoggedInUser().getId());
        if (pet == null) {
            throw new InformationNotFoundException("pet with id " + petId + " not found");
        } else {
            pet.setName(petObject.getName());
            pet.setType(petObject.getType());
            pet.setBreed(petObject.getBreed());
            pet.setAge(petObject.getAge());
            pet.setIsVaccinated(petObject.getIsVaccinated());
            pet.setPetStatus(petObject.getPetStatus());
            pet.setUser(PetService.getCurrentLoggedInUser());
            return petRepository.save(petObject);
        }
    }

    public void deletePet(Long petId) {
        Pet pet = petRepository.findByIdAndUserId(petId, PetService.getCurrentLoggedInUser().getId());
        if (pet == null) {
            throw new InformationNotFoundException("pet with id " + petId + " not found");
        } else {
            petRepository.deleteById(petId);
        }
    }

    public List<Pet> getAllPets() {
        List<Pet> pets = petRepository.findAll();

        if (pets.isEmpty()) {
            throw new InformationNotFoundException("no pets were found.");
        } else {
            return pets;
        }
    }
}
