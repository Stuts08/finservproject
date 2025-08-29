package com.example.bfhl;

import com.example.bfhl.service.HiringChallengeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements ApplicationRunner {

    private final HiringChallengeService service;

    @Value("${app.candidate.name}") private String name;
    @Value("${app.candidate.regNo}") private String regNo;
    @Value("${app.candidate.email}") private String email;

    public StartupRunner(HiringChallengeService service) {
        this.service = service;
    }

    @Override
    public void run(ApplicationArguments args) {
        service.runFlow(name, regNo, email);
    }
}
