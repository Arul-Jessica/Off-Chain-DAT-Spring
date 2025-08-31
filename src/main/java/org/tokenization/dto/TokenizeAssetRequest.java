package org.tokenization.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TokenizeAssetRequest {
    private Long issuerPartyId;
    private String name;
    private String symbol;
    private BigDecimal totalSupply;
}