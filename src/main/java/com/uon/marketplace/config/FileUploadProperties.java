package com.uon.marketplace.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "file")
public class FileUploadProperties {
    private String uploadDir;
    private String uploadDirAbsolute;

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String getUploadDirAbsolute() {
        return uploadDirAbsolute;
    }

    public void setUploadDirAbsolute(String uploadDirAbsolute) {
        this.uploadDirAbsolute = uploadDirAbsolute;
    }
}
