package com.example.bfhl.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateWebhookRequest {
    @NotBlank private String name;
    @NotBlank private String regNo;
    @Email @NotBlank private String email;
}
