package com.ga.petadoption.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyEmailRequest {
    private String token;
}
