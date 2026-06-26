package Afford.affordBackend.middleware;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class LoggingMiddleWare {
    private final RestTemplate restTemplate;

    @Value("${testserver.base-url}")
    private String baseUrl;

    public static volatile String cachedToken = null;

    public LoggingMiddleWare(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public static synchronized void setAuthToken(String token) {
        cachedToken = token;
    }

    @Async
    public void Log(String stack, String level, String packageName, String message) {

        String cleanStack = stack.toLowerCase();
        String cleanLevel = level.toLowerCase();
        String cleanPackage = packageName.toLowerCase();

        System.out.printf("[%s] [%s] -> %s%n", cleanLevel.toUpperCase(), cleanPackage, message);

        if (cachedToken == null) {
            System.err.println("Not Authenticated yet.");
            return;
        }

        try {
            String url = baseUrl + "/logs";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + cachedToken);

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("stack", cleanStack);
            requestBody.put("level", cleanLevel);
            requestBody.put("package", cleanPackage);
            requestBody.put("message", message);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            restTemplate.postForEntity(url, entity, Map.class);
        } catch (Exception e) {
            System.err.println("Test server logging fault: " + e.getMessage());
        }

        }
}
