package org.tokenization.dto;

import lombok.Data;

@Data
public class PartyResponse {
    private Long id;
    private String name;
    private String kycLevel;
    private String status;
}