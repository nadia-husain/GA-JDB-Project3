package com.ga.petadoption.model.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ga.petadoption.model.AdoptionRequest;
import com.ga.petadoption.model.enums.AdoptionRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"id", "userId", "petId", "status", "createdAt", "updatedAt"})
public class AdoptionRequestResponse {
    private Long id;
    private Long userId;
    private Long petId;
    private AdoptionRequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AdoptionRequestResponse(AdoptionRequest ar) {
        this.id = ar.getId();
        this.userId = ar.getUser().getId();
        this.petId = ar.getPet().getId();
        this.status = ar.getStatus();
        this.createdAt = ar.getCreatedAt();
        this.updatedAt = ar.getUpdatedAt();
    }
}
