package Afford.affordBackend.controller;

import Afford.affordBackend.DTOs.ScheduleResult;
import Afford.affordBackend.services.VehicleSchedulerService;
import Afford.affordBackend.middleware.LoggingMiddleWare;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/scheduler")
public class VehicleSchedulerController {

    private final VehicleSchedulerService schedulerService;
    private final LoggingMiddleWare logMiddleware;

    public VehicleSchedulerController(VehicleSchedulerService schedulerService, LoggingMiddleWare logMiddleware) {
        this.schedulerService = schedulerService;
        this.logMiddleware = logMiddleware;
    }

    @GetMapping("/optimize")
    public ResponseEntity<List<ScheduleResult>> getOptimalSchedule() {
        logMiddleware.Log("backend", "info", "controller", "Received routing request at GET /api/v1/scheduler/optimize");
        List<ScheduleResult> results = schedulerService.computeOptimalSchedules();
        return ResponseEntity.ok(results);
    }
}