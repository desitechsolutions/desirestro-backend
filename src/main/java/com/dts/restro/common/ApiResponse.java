package com.dts.restro.common;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Uniform API response envelope used by all controllers.
 *
 * @param <T> the type of the response payload
 */
@Data
@AllArgsConstructor
public class ApiResponse<T> {

    /** "success" or "error" */
    private String status;

    /** Human-readable message */
    private String message;

    /** Response payload (may be null for error responses) */
    private T data;

    /** Convenience factory for successful responses */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>("success", message, data);
    }

    /** Convenience factory for successful responses without a custom message */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", "Success", data);
    }

    /** Convenience factory for error responses */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("error", message, null);
    }
}
