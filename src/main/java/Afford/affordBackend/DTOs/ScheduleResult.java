package Afford.affordBackend.DTOs;
import java.util.List;

public class ScheduleResult {
    public int depotId;
    public int maxMechanicHoursBudget;
    public int totalDurationSpent;
    public int totalOperationalImpactScore;
    public List<String> selectedTaskIds;
}