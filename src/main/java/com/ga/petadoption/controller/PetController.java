package com.ga.petadoption.controller;

import com.ga.petadoption.model.Pet;
import com.ga.petadoption.service.PetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/pets/")
public class PetController {
    private PetService petService;

    @Autowired
    public void setPetService(PetService petService) {
        this.petService = petService;
    }

    @GetMapping("all")
    public List<Pet> getPets() {
        System.out.println("calling getPets ==>");
        return petService.getAllPets();
    }

    @GetMapping(path = "{petId}")
    public Pet getPet(@PathVariable Long petId) {
        System.out.println("calling getPet ==>");
        return petService.getPetById(petId);
    }

    @PostMapping("new")
    public Pet createPet(@RequestBody Pet petObject) {
        System.out.println("calling createPet ==>");
        return petService.createPet(petObject);
    }

    @PutMapping("{petId}")
    public Pet updatePet(@PathVariable(value = "petId") Long petId, @RequestBody Pet petObject) {
        System.out.println("calling updatePet ==>");
        return petService.updatePet(petId, petObject);
    }

    @DeleteMapping("{petId}")
    public String deletePet(@PathVariable(value = "petId") Long petId) {
        System.out.println("calling deletePet ==>");
        petService.deletePet(petId);
        return "Pet deleted successfully";
    }

}
