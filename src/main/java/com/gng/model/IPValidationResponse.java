package com.gng.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Model for IP Validation.
 */
@Getter
@Setter
@AllArgsConstructor
public class IPValidationResponse {

    private String query;
    private String country;
    private String countryCode;
    private String isp;
}
