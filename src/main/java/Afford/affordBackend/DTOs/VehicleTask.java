package Afford.affordBackend.DTOs;


import com.fasterxml.jackson.annotation.JsonProperty;

public class VehicleTask {
    @JsonProperty("TaskID")
    private String taskId;

    @JsonProperty("Duration")
    private int duration;

    @JsonProperty("Impact")
    private int impact;

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
    public int getImpact() { return impact; }
    public void setImpact(int impact) { this.impact = impact; }
}