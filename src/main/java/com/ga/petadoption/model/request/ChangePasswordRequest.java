package com.ga.petadoption.model.request;

import lombok.Getter;

@Getter
public class ChangePasswordRequest {
    private String oldPassword;
    private String newPassword;
}
