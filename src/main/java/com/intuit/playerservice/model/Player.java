package com.intuit.playerservice.model;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Player {
    private String playerID;
    private int birthYear;
    private int birthMonth;
    private int birthDay;
    private String birthCountry;
    private String birthState;
    private String birthCity;
    private Integer deathYear;
    private Integer deathMonth;
    private Integer deathDay;
    private String deathCountry;
    private String deathState;
    private String deathCity;
    private String nameFirst;
    private String nameLast;
    private String nameGiven;
    private double weight;
    private double height;
    private String bats;

    @JsonProperty("throws")
    private String playerThrows;
    private String debut;
    private String finalGame;
    private String retroID;
    private String bbrefID;
}