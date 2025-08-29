package com.example.bfhl.persistence;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "submission_result")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class SubmissionResult {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String regNo;
    private Integer questionNumber;
    @Column(length = 4000)
    private String finalQuery;
    private String webhookUrl;
    private String accessTokenMasked;
    private Instant submittedAt;
    private boolean submissionSuccess;
    @Column(length = 2000)
    private String submissionError;
}
