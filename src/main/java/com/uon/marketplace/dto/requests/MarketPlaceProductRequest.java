package com.uon.marketplace.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MarketPlaceProductRequest {
    private Long sellerId;
    private String productDescription;
    private String productImageUrl;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private String condition;
    private String category;
}
