package com.uon.marketplace.dto.responses;

import java.time.LocalDateTime;

public class SavedProductWithImageResponse {
    private Long savedId;
    private Long userId;
    private Long productId;
    private LocalDateTime savedDate;
    private String productImageUrl;

    public SavedProductWithImageResponse(Long savedId, Long userId, Long productId, LocalDateTime savedDate, String productImageUrl) {
        this.savedId = savedId;
        this.userId = userId;
        this.productId = productId;
        this.savedDate = savedDate;
        this.productImageUrl = productImageUrl;
    }

    public Long getSavedId() { return savedId; }
    public void setSavedId(Long savedId) { this.savedId = savedId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public LocalDateTime getSavedDate() { return savedDate; }
    public void setSavedDate(LocalDateTime savedDate) { this.savedDate = savedDate; }
    public String getProductImageUrl() { return productImageUrl; }
    public void setProductImageUrl(String productImageUrl) { this.productImageUrl = productImageUrl; }
}
