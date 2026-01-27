package com.nexbank.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    private String message;
    
    private String code;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private String path;
    
    /**
     * Constructor de conveniencia
     */
    public ErrorResponse(String message, String code) {
        this.message = message;
        this.code = code;
        this.timestamp = LocalDateTime.now();
    }
}