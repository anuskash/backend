package com.uon.marketplace.dto.requests;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request model for multiple image upload via multipart/form-data
 */
public class MultipleImageUploadRequest {

    @ArraySchema(schema = @Schema(type = "string", format = "binary"))
    @Schema(description = "Array of image files to upload (max 10)")
    private MultipartFile[] files;

    public MultipartFile[] getFiles() {
        return files;
    }

    public void setFiles(MultipartFile[] files) {
        this.files = files;
    }
}
