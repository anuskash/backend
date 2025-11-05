package com.uon.marketplace.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnlockAccountRequest {
    private String email;
    private String code; // 6-digit unlock code
}
