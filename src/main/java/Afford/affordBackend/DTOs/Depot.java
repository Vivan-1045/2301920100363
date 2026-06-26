package Afford.affordBackend.DTOs;


import com.fasterxml.jackson.annotation.JsonProperty;

public class Depot {
    @JsonProperty("ID")
    private int id;

    @JsonProperty("MechanicHours")
    private int mechanicHours;


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getMechanicHours() { return mechanicHours; }
    public void setMechanicHours(int mechanicHours) { this.mechanicHours = mechanicHours; }
}
