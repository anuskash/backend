package com.uon.marketplace.services;

import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class ImageUploadService {

    @Value("${file.upload-dir-absolute}")
    private String uploadDir;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int MIN_DIMENSION = 300;
    private static final int MAX_DIMENSION = 4000;
    private static final int THUMBNAIL_SIZE = 400;

    /**
     * Validates and uploads a single image file
     * @param file the multipart file to upload
     * @return the relative URL path to the uploaded image
     * @throws IOException if upload fails
     */
    public String uploadImage(MultipartFile file) throws IOException {
        // Validate file
        validateImage(file);

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + "." + extension;

        // Save original file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Generate thumbnail (optional but recommended)
        generateThumbnail(filePath, uploadPath, uniqueFilename);

        // Return relative URL path
        return "/uploads/products/" + uniqueFilename;
    }

    /**
     * Uploads multiple images
     * @param files array of multipart files
     * @return list of image URLs
     * @throws IOException if upload fails
     */
    public List<String> uploadMultipleImages(MultipartFile[] files) throws IOException {
        List<String> imageUrls = new ArrayList<>();
        
        if (files.length > 10) {
            throw new IllegalArgumentException("Maximum 10 images allowed per product");
        }

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String imageUrl = uploadImage(file);
                imageUrls.add(imageUrl);
            }
        }

        return imageUrls;
    }

    /**
     * Deletes an image file
     * @param imageUrl the URL path of the image to delete
     * @return true if deleted successfully
     */
    public boolean deleteImage(String imageUrl) {
        try {
            // Extract filename from URL
            String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(uploadDir, filename);
            Path thumbnailPath = Paths.get(uploadDir, "thumb_" + filename);

            // Delete original and thumbnail
            Files.deleteIfExists(filePath);
            Files.deleteIfExists(thumbnailPath);

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Validates image file
     */
    private void validateImage(MultipartFile file) throws IOException {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 5MB");
        }

        // Check file extension
        String filename = file.getOriginalFilename();
        String extension = getFileExtension(filename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file type. Only JPEG, PNG, and WEBP are allowed");
        }

        // Check image dimensions
        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
            throw new IllegalArgumentException("Invalid image file");
        }

        int width = image.getWidth();
        int height = image.getHeight();

        if (width < MIN_DIMENSION || height < MIN_DIMENSION) {
            throw new IllegalArgumentException(
                String.format("Image dimensions too small. Minimum %dx%d pixels required", MIN_DIMENSION, MIN_DIMENSION)
            );
        }

        if (width > MAX_DIMENSION || height > MAX_DIMENSION) {
            throw new IllegalArgumentException(
                String.format("Image dimensions too large. Maximum %dx%d pixels allowed", MAX_DIMENSION, MAX_DIMENSION)
            );
        }
    }

    /**
     * Generates a thumbnail for the uploaded image
     */
    private void generateThumbnail(Path originalPath, Path uploadPath, String filename) {
        try {
            BufferedImage originalImage = ImageIO.read(originalPath.toFile());
            
            // Resize image to thumbnail size
            BufferedImage thumbnail = Scalr.resize(
                originalImage,
                Scalr.Method.QUALITY,
                Scalr.Mode.FIT_TO_WIDTH,
                THUMBNAIL_SIZE,
                THUMBNAIL_SIZE,
                Scalr.OP_ANTIALIAS
            );

            // Save thumbnail
            String extension = getFileExtension(filename);
            File thumbnailFile = uploadPath.resolve("thumb_" + filename).toFile();
            ImageIO.write(thumbnail, extension, thumbnailFile);

        } catch (IOException e) {
            // Log error but don't fail the upload
            System.err.println("Failed to generate thumbnail: " + e.getMessage());
        }
    }

    /**
     * Extracts file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
