package com.gng.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Model for Entry file.
 */
@Getter
@Setter
@AllArgsConstructor
public class Entry {

    @JsonProperty("UUID")
    private String uuid;

    @JsonProperty("ID")
    private String id;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Likes")
    private String likes;

    @JsonProperty("Transport")
    private String transport;

    @JsonProperty("Avg Speed")
    private double avgSpeed;

    @JsonProperty("Top Speed")
    private double topSpeed;

}
