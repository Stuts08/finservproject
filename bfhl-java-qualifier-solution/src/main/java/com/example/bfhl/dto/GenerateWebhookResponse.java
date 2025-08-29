package com.example.bfhl.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateWebhookResponse {
    private String webhook;
    private String accessToken;
}
