package com.example.bfhl.service;

import com.example.bfhl.dto.GenerateWebhookRequest;
import com.example.bfhl.dto.GenerateWebhookResponse;
import com.example.bfhl.dto.SubmitRequest;
import com.example.bfhl.persistence.SubmissionResult;
import com.example.bfhl.persistence.SubmissionResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class HiringChallengeService {

    private final WebClient webClient;
    private final SubmissionResultRepository repository;

    @Value("${bfhl.generateWebhookPath}")
    private String generateWebhookPath;
    @Value("${bfhl.submitPath}")
    private String submitPath;

    public void runFlow(String name, String regNo, String email) {
        SubmissionResult.SubmissionResultBuilder resultBuilder = SubmissionResult.builder()
                .regNo(regNo)
                .submittedAt(Instant.now());

        try {
            // 1) Generate webhook & token
            GenerateWebhookRequest req = GenerateWebhookRequest.builder()
                    .name(name).regNo(regNo).email(email).build();

            GenerateWebhookResponse resp = webClient.post()
                    .uri(generateWebhookPath)
                    .body(BodyInserters.fromValue(req))
                    .retrieve()
                    .bodyToMono(GenerateWebhookResponse.class)
                    .block();

            if (resp == null || resp.getWebhook() == null || resp.getAccessToken() == null) {
                throw new IllegalStateException("Invalid response from generateWebhook");
            }
            log.info("Generated webhook: {}", resp.getWebhook());
            resultBuilder.webhookUrl(resp.getWebhook());
            resultBuilder.accessTokenMasked(mask(resp.getAccessToken()));

            // 2) Determine question number from regNo last two digits
            int qn = computeQuestionNumber(regNo);
            resultBuilder.questionNumber(qn);
            log.info("Determined question number: {}", qn);

            // 3) Load final SQL from resources
            String sqlPath = qn == 1 ? "queries/question1.sql" : "queries/question2.sql";
            String finalQuery = readResource(sqlPath).trim();
            if (finalQuery.isBlank()) {
                throw new IllegalStateException("Final SQL is blank. Please fill " + sqlPath);
            }
            resultBuilder.finalQuery(finalQuery);
//            log.debug("Final SQL:
//{}", finalQuery);

            // 4) Submit final SQL using Authorization: <accessToken>
            SubmitRequest submit = SubmitRequest.builder().finalQuery(finalQuery).build();

            // The submit URL in instructions is a fixed path, but they also give a "webhook" URL.
            // We'll prefer the provided webhook URL if present; otherwise use submitPath.
            String submitUrl = resp.getWebhook() != null && !resp.getWebhook().isBlank()
                    ? resp.getWebhook()
                    : submitPath;

            String ack = webClient.post()
                    .uri(submitUrl)
                    .headers(h -> h.set("Authorization", resp.getAccessToken()))
                    .body(BodyInserters.fromValue(submit))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Submission ACK: {}", ack);
            resultBuilder.submissionSuccess(true);

        } catch (Exception e) {
            log.error("Flow failed", e);
            resultBuilder.submissionSuccess(false).submissionError(e.toString());
        } finally {
            repository.save(resultBuilder.build());
        }
    }

    static String mask(String token) {
        if (token == null || token.length() < 10) return "****";
        return token.substring(0, 6) + "..." + token.substring(token.length()-4);
    }

    static int computeQuestionNumber(String regNo) {
        // Extract last two digits from any digits in regNo
        Matcher m = Pattern.compile("(\\d{2})(?!.*\\d)").matcher(regNo);
        int lastTwo = 0;
        if (m.find()) {
            lastTwo = Integer.parseInt(m.group(1));
        } else {
            // fallback: take last digit
            Matcher m2 = Pattern.compile("(\\d)(?!.*\\d)").matcher(regNo);
            if (m2.find()) lastTwo = Integer.parseInt(m2.group(1));
        }
        return (lastTwo % 2 == 1) ? 1 : 2;
    }

    static String readResource(String cp) throws Exception {
        ClassPathResource res = new ClassPathResource(cp);
        return StreamUtils.copyToString(res.getInputStream(), StandardCharsets.UTF_8);
    }
}
