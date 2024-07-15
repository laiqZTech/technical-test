package com.gng.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;


/**
 * Entity to store request information.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
public class FileRequestLog {

    @Id
    private String id;

    private String uri;
    private LocalDateTime timestamp;
    private int httpResponseCode;
    private String ipAddress;
    private String countryCode;
    private String ipProvider;
    private long timeLapsed;

}
