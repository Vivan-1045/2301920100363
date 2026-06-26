package Afford.affordBackend.services;

import Afford.affordBackend.DTOs.*;
import Afford.affordBackend.middleware.LoggingMiddleWare;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class VehicleSchedulerService {

    private final RestTemplate restTemplate;
    private final LoggingMiddleWare logMiddleware;

    @Value("${testserver.base-url}")
    private String baseUrl;

    public VehicleSchedulerService(RestTemplate restTemplate, LoggingMiddleWare logMiddleware) {
        this.restTemplate = restTemplate;
        this.logMiddleware = logMiddleware;
    }

    private HttpHeaders getAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + LoggingMiddleWare.cachedToken);
        return headers;
    }

    public List<ScheduleResult> computeOptimalSchedules() {
        logMiddleware.Log("backend", "info", "service", "Initiating global vehicle maintenance scheduling optimization sequence.");


        DepotResponse depotData;
        VehicleResponse vehicleData;
        try {
            HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());

            ResponseEntity<DepotResponse> depotRes = restTemplate.exchange(
                    baseUrl + "/depots", HttpMethod.GET, entity, DepotResponse.class);
            depotData = depotRes.getBody();

            ResponseEntity<VehicleResponse> vehicleRes = restTemplate.exchange(
                    baseUrl + "/vehicles", HttpMethod.GET, entity, VehicleResponse.class);
            vehicleData = vehicleRes.getBody();

            logMiddleware.Log("backend", "info", "service", "Successfully fetched depot and vehicle records from protected test server.");
        } catch (Exception e) {
            logMiddleware.Log("backend", "error", "service", "Failed to retrieve initial dataset from remote server: " + e.getMessage());
            throw new RuntimeException("External API data extraction failure", e);
        }

        List<ScheduleResult> optimalSchedules = new ArrayList<>();
        if (depotData == null || depotData.depots == null || vehicleData == null || vehicleData.vehicles == null) {
            return optimalSchedules;
        }


        List<VehicleTask> tasks = vehicleData.vehicles;
        int n = tasks.size();

        for (Depot depot : depotData.depots) {
            int budget = depot.getMechanicHours();
            logMiddleware.Log("backend", "debug", "service", "Processing Depot ID: " + depot.getId() + " with hour budget: " + budget);

            int[][] dp = new int[n + 1][budget + 1];

            for (int i = 1; i <= n; i++) {
                VehicleTask task = tasks.get(i - 1);
                for (int w = 0; w <= budget; w++) {
                    if (task.getDuration() <= w) {
                        dp[i][w] = Math.max(task.getImpact() + dp[i - 1][w - task.getDuration()], dp[i - 1][w]);
                    } else {
                        dp[i][w] = dp[i - 1][w];
                    }
                }
            }

            List<String> selectedTasks = new ArrayList<>();
            int w = budget;
            int totalDuration = 0;
            for (int i = n; i > 0 && w > 0; i--) {
                if (dp[i][w] != dp[i - 1][w]) {
                    VehicleTask selected = tasks.get(i - 1);
                    selectedTasks.add(selected.getTaskId());
                    totalDuration += selected.getDuration();
                    w -= selected.getDuration();
                }
            }

            ScheduleResult result = new ScheduleResult();
            result.depotId = depot.getId();
            result.maxMechanicHoursBudget = budget;
            result.totalDurationSpent = totalDuration;
            result.totalOperationalImpactScore = dp[n][budget];
            result.selectedTaskIds = selectedTasks;

            optimalSchedules.add(result);
            System.out.println("Final Result : "+ Arrays.asList(result));
            logMiddleware.Log("backend", "info", "service", "Optimized Depot ID " + depot.getId() + ": Score maximized to " + result.totalOperationalImpactScore);
        }

        return optimalSchedules;
    }
}