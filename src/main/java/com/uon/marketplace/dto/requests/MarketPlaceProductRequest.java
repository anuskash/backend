package com.uon.marketplace.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MarketPlaceProductRequest {
    private Long sellerId;
    private String productDescription;
    private List<String> imageUrls;
    private String productName;
    private BigDecimal price;
    private String condition;
    private String category;
}
