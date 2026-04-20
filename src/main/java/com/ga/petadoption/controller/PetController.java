package com.ga.petadoption.controller;

import com.ga.petadoption.model.Pet;
import com.ga.petadoption.service.PetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping(path = "api/pet/")
public class PetController {
    private PetService petService;

    @Autowired
    public void setPetService(PetService petService) {
        this.petService = petService;
    }

    @PostMapping(value = "new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Pet createPet(
            @RequestPart("pet") Pet petObject,
            @RequestPart(value = "photo", required = false) MultipartFile photo) throws IOException {
        System.out.println("calling createPet ==>");
        return petService.createPet(petObject, photo);
    }

    @GetMapping(path = "{petId}")
    public Pet getPetById(@PathVariable Long petId) {
        System.out.println("calling getPetById ==>");
        return petService.getPetById(petId);
    }

    @GetMapping("all")
    public List<Pet> getAllPets() {
        System.out.println("calling getAllPets ==>");
        return petService.getAllPets();
    }

    @PatchMapping(value = "{petId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Pet updatePet(
            @PathVariable Long petId,
            @RequestPart("pet") Pet petObject,
            @RequestPart(value = "photo", required = false) MultipartFile photo) throws IOException {
        System.out.println("calling updatePet ==>");
        return petService.updatePet(petId, petObject, photo);
    }

    @DeleteMapping("{petId}")
    public String deletePet(@PathVariable(value = "petId") Long petId) {
        System.out.println("calling deletePet ==>");
        petService.deletePet(petId);
        return "Pet deleted successfully";
    }

    @GetMapping("/{petId}/photo")
    public ResponseEntity<byte[]> getPetPhoto(@PathVariable Long petId) {
        Pet pet = petService.getPetById(petId);
        byte[] imageBytes = Base64.getDecoder().decode(pet.getPhoto());
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(imageBytes);
    }
}
