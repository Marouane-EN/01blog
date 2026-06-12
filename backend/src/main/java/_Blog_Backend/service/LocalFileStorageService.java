package _Blog_Backend.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalFileStorageService {

    private final String PROFILE_DIR = "uploads/profiles/";
    private final String POST_DIR = "uploads/posts/";

    public String saveProfilePicture(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(PROFILE_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new IllegalArgumentException("Invalid image file or hidden payload detected.");
        }

        String safeFilename = UUID.randomUUID().toString() + ".jpg";
        File targetFile = new File(PROFILE_DIR + safeFilename);

        ImageIO.write(originalImage, "jpg", targetFile);

        return "/uploads/profiles/" + safeFilename;
    }

    public String savePostMedia(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(POST_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed for posts.");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = ".jpg";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String safeFilename = UUID.randomUUID().toString() + extension;
        Path targetLocation = uploadPath.resolve(safeFilename);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
        }

        return "/uploads/posts/" + safeFilename;
    }

    public void deleteFile(String fileUrl) {
        try {

            String relativePath = fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;

            Path filePath = Paths.get(relativePath).normalize();

            Files.deleteIfExists(filePath);

        } catch (Exception e) {

            System.err.println("Failed to delete physical file: " + e.getMessage());
        }
    }

    public Resource loadFileAsResource(String directory, String filename) {
        try {
            Path filePath = Paths.get(directory).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found or not readable");
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not read file: " + filename, e);
        }
    }
}