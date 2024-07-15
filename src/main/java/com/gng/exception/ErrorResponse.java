package com.gng.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Global error response format.
 */
@Getter
@Setter
@AllArgsConstructor
public class ErrorResponse {

    private int status;

    private String message;

}
