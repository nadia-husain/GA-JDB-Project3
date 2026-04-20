package com.ga.petadoption.controller;

import com.ga.petadoption.model.AdoptionRequest;
import com.ga.petadoption.model.enums.AdoptionRequestStatus;
import com.ga.petadoption.model.response.AdoptionRequestResponse;
import com.ga.petadoption.security.MyUserDetails;
import com.ga.petadoption.service.AdoptionRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @PostMapping("/{petId}/new")
    public AdoptionRequestResponse createAdoptionRequest(
            @PathVariable Long petId,
            @AuthenticationPrincipal MyUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        AdoptionRequest ar = adoptionRequestService.createAdoptionRequest(petId, userId);
        return new AdoptionRequestResponse(ar);
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

    @PatchMapping(path = "{adoptionRequestId}/status")
    public AdoptionRequestResponse updateAdoptionRequestStatus(
            @PathVariable Long adoptionRequestId,
            @RequestParam AdoptionRequestStatus status) {
        System.out.println("calling updateAdoptionRequest ==>");
        AdoptionRequest ar = adoptionRequestService.updateAdoptionRequestStatus(adoptionRequestId, status);
        return new AdoptionRequestResponse(ar);
    }

    @DeleteMapping("{adoptionRequestId}")
    public String deleteAdoptionRequest(@PathVariable(value = "adoptionRequestId") Long adoptionRequestId) {
        System.out.println("calling deleteAdoptionRequest ==>");
        adoptionRequestService.deleteAdoptionRequest(adoptionRequestId);
        return "Adoption Request ID:" + adoptionRequestId + " deleted successfully.";
    }
}
