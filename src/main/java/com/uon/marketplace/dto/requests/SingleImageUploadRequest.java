package com.uon.marketplace.dto.requests;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request model for single image upload via multipart/form-data
 */
public class SingleImageUploadRequest {

    @Schema(description = "Image file to upload", type = "string", format = "binary")
    private MultipartFile file;

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
}
