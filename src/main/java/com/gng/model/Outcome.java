package com.gng.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Model for Outcome.
 */
@Getter
@Setter
@AllArgsConstructor
public class Outcome {

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Transport")
    private String transport;

    @JsonProperty("Top Speed")
    private double topSpeed;

}
