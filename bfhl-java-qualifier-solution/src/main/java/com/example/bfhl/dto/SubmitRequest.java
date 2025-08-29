package com.example.bfhl.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitRequest {
    private String finalQuery;
}
