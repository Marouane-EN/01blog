package _Blog_Backend.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalFileStorageService {

    private final String UPLOAD_DIR = "uploads/profiles/";

    public String saveProfilePicture(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new IllegalArgumentException("Invalid image file or hidden payload detected.");
        }

        String safeFilename = UUID.randomUUID().toString() + ".jpg";
        File targetFile = new File(UPLOAD_DIR + safeFilename);

        ImageIO.write(originalImage, "jpg", targetFile);

        return "/uploads/profiles/" + safeFilename;
    }
}