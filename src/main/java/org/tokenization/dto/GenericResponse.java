package com.tokenization.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor // A Lombok annotation to create a constructor with all fields
public class GenericResponse {
    private String message;
}