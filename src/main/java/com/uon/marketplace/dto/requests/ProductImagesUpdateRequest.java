package com.uon.marketplace.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductImagesUpdateRequest {
    private List<String> imageUrls;
}
