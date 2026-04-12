package com.ga.petadoption.controller;

import com.ga.petadoption.model.AdoptionRequest;
import com.ga.petadoption.service.AdoptionRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/adoption/")
public class AdoptionRequestController {
    private AdoptionRequestService adoptionRequestService;

    @Autowired
    public void setAdoptionRequestService(AdoptionRequestService adoptionRequestService) {
        this.adoptionRequestService = adoptionRequestService;
    }

    @PostMapping("new")
    public AdoptionRequest createAdoptionRequest(@PathVariable Long petId, Long userId) {
        System.out.println("calling createAdoptionRequest ==>");
        return adoptionRequestService.createAdoptionRequest(petId, userId);
    }

    @GetMapping("all")
    public List<AdoptionRequest> getAllAdoptionRequests() {
        System.out.println("calling getAllAdoptionRequests ==>");
        return adoptionRequestService.getAllAdoptionRequests();
    }

    @GetMapping(path = "{adoptionRequestId}")
    public AdoptionRequest getAdoptionRequestById(@PathVariable Long adoptionRequestId) {
        System.out.println("calling getAdoptionRequestById ==>");
        return adoptionRequestService.getAdoptionRequestById(adoptionRequestId);
    }

    @DeleteMapping("{adoptionRequestId}")
    public String deleteAdoptionRequest(@PathVariable(value = "adoptionRequestId") Long adoptionRequestId) {
        System.out.println("calling deleteAdoptionRequest ==>");
        adoptionRequestService.deleteAdoptionRequest(adoptionRequestId);
        return "Adoption Request deleted successfully";
    }

    @PostMapping("/simulate/{petId}")
    public String simulate(@PathVariable Long petId) {
        adoptionRequestService.simulateConcurrentAdoptions(petId);
        return "Simulation started!";
    }
}
