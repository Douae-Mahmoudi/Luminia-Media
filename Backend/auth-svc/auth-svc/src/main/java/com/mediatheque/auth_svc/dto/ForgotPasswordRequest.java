package com.mediatheque.auth_svc.dto;

import lombok.Data;

@Data
public class ForgotPasswordRequest {
    private String email;
}