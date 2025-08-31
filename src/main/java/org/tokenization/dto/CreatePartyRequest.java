package org.tokenization.dto;
import lombok.Data;

@Data
public class CreatePartyRequest {
    private String name;
    private String kycLevel;
    // Note: We are not including username/password here, following the new plan to handle auth later.
}