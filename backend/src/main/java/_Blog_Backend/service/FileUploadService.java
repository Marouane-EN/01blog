package _Blog_Backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final Cloudinary cloudinary;
    private final Tika tika = new Tika();

    private static final Set<String> IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp");
    private static final Set<String> VIDEO_TYPES = Set.of(
            "video/mp4", "video/webm", "video/quicktime");

    public String uploadFile(MultipartFile file) throws IOException {
        return sendToCloudinary(file, validate(file, true));
    }

    public String uploadImageOnly(MultipartFile file) throws IOException { // for avatars
        return sendToCloudinary(file, validate(file, false));
    }

    public List<String> uploadMultipleFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return new ArrayList<>();
        }

        List<MultipartFile> nonEmpty = files.stream().filter(f -> !f.isEmpty()).toList();

        // Phase 1: validate everything before touching Cloudinary
        List<String> resourceTypes = new ArrayList<>();
        for (MultipartFile file : nonEmpty) {
            resourceTypes.add(validate(file, true));
        }

        // Phase 2: upload, and clean up if anything fails midway
        List<String> uploadedUrls = new ArrayList<>();
        try {
            for (int i = 0; i < nonEmpty.size(); i++) {
                uploadedUrls.add(sendToCloudinary(nonEmpty.get(i), resourceTypes.get(i)));
            }
        } catch (IOException | RuntimeException e) {
            uploadedUrls.forEach(this::deleteFileByUrl);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Upload failed, no files were saved.", e);
        }
        return uploadedUrls;
    }

    /** Checks the real file type via magic bytes; returns "image" or "video". */
    private String validate(MultipartFile file, boolean allowVideo) {
        String realType;
        try (InputStream in = new BufferedInputStream(file.getInputStream())) {
            realType = tika.detect(in);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not read the uploaded file.");
        }

        if (IMAGE_TYPES.contains(realType))
            return "image";
        if (allowVideo && VIDEO_TYPES.contains(realType))
            return "video";

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Unsupported file type: only images" + (allowVideo ? " and videos" : "") + " are allowed.");
    }

    private String sendToCloudinary(MultipartFile file, String resourceType) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "resource_type", resourceType,
                "allowed_formats", "jpg,jpeg,png,gif,webp,mp4,webm,mov"));
        return uploadResult.get("secure_url").toString();
    }

    public void deleteFileByUrl(String mediaUrl) {
        if (mediaUrl == null || mediaUrl.trim().isEmpty()) {
            return;
        }

        try {
            String publicId = mediaUrl.substring(
                    mediaUrl.lastIndexOf("/") + 1,
                    mediaUrl.lastIndexOf("."));
            String resourceType = mediaUrl.contains("/video/upload/") ? "video" : "image";

            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", resourceType));

        } catch (Exception e) {
            System.err.println("WARNING: Failed to delete orphaned file: " + e.getMessage());
        }
    }
}