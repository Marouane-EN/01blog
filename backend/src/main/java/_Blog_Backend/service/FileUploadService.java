package _Blog_Backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final Cloudinary cloudinary;

    public String uploadFile(MultipartFile file) throws IOException {
        
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), 
                ObjectUtils.asMap("resource_type", "auto")); 
        
        return uploadResult.get("secure_url").toString();
    }

    public List<String> uploadMultipleFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> uploadedUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                try {
                    uploadedUrls.add(this.uploadFile(file));
                } catch (IOException e) {
                    System.err.println("Failed to upload an file in the batch: " + e.getMessage());
                }
            }
        }
        return uploadedUrls;
    }

    public void deleteFileByUrl(String mediaUrl) {
        if (mediaUrl == null || mediaUrl.trim().isEmpty()) {
            return;
        }

        try {
            String publicId = mediaUrl.substring(
                    mediaUrl.lastIndexOf("/") + 1,
                    mediaUrl.lastIndexOf("."));

            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());

        } catch (Exception e) {
            System.err.println("WARNING: Failed to delete orphaned file: " + e.getMessage());
        }
    }
}