package Afford.affordBackend.utils;

import Afford.affordBackend.middleware.LoggingMiddleWare;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class ServerSetupRunner implements CommandLineRunner {
    private final RestTemplate restTemplate;
    private final LoggingMiddleWare logMiddleware;

    @Value("${testserver.base-url}")
    private String baseUrl;

    @Value("${testserver.access-code}")
    private String accessCode;

    @Value("${testserver.roll-no}")
    private String rollNo;

    @Value("${testserver.email}")
    private String email;

    @Value("${testserver.name}")
    private String name;

    @Value("${testserver.github-username}")
    private String githubUsername;

    @Value("${testserver.mobile-no}")
    private String mobileNo;

    @Value("${testserver.client-id:}")
    private String clientId;

    @Value("${testserver.client-secret:}")
    private String clientSecret;

    public ServerSetupRunner(RestTemplate restTemplate, LoggingMiddleWare logMiddleware) {
        this.restTemplate = restTemplate;
        this.logMiddleware = logMiddleware;
    }

    @Override
    public void run(String... args) {
        System.out.println("=== INITIALIZING EVALUATION SERVER SYSTEM ENVIRONMENT ===");

        if (clientId == null || clientId.trim().isEmpty()) {
            executeRegistration();
            return;
        }

        executeAuthenticationAndVerification();
    }

    private void executeRegistration() {
        try {
            String url = baseUrl + "/register";

            Map<String, String> body = new HashMap<>();
            body.put("email", email);
            body.put("name", name);
            body.put("mobileNo", mobileNo);
            body.put("githubUsername", githubUsername);
            body.put("rollNo", rollNo);
            body.put("accessCode", accessCode);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            System.out.println("Registering application on core server endpoint...");
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> resBody = response.getBody();
                System.out.println("REGISTRATION COMPLETE");

                System.out.println("testserver.client-id=" + resBody.get("clientID"));
                System.out.println("testserver.client-secret=" + resBody.get("clientSecret"));
            }
        } catch (Exception e) {
            System.err.println("Registration not completed: " + e.getMessage());
        }
    }

    private void executeAuthenticationAndVerification() {
        try {
            String url = baseUrl + "/auth";

            Map<String, String> body = new HashMap<>();
            body.put("email", email);
            body.put("name", name);
            body.put("rollNo", rollNo);
            body.put("accessCode", accessCode);
            body.put("clientID", clientId);
            body.put("clientSecret", clientSecret);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            System.out.println("Fetching validation context token...");
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String token = (String) response.getBody().get("access_token");
                LoggingMiddleWare.setAuthToken(token);

                logMiddleware.Log("backend", "info", "middleware", "Ecosystem context handshake validated successfully.");
            }
        } catch (Exception e) {
            System.err.println("Authentication exception: " + e.getMessage());
        }
    }
}