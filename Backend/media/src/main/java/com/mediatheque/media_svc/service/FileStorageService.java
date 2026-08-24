package com.mediatheque.media_svc.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private static final java.util.Set<String> IMAGE_TYPES = java.util.Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final java.util.Set<String> VIDEO_TYPES = java.util.Set.of(
            "video/mp4", "video/mkv", "video/webm", "video/avi", "video/quicktime"
    );
    private static final java.util.Set<String> AUDIO_TYPES = java.util.Set.of(
            "audio/mpeg", "audio/mp3", "audio/wav", "audio/x-m4a", "audio/ogg",
            "audio/aac", "audio/mp4"
    );
    private static final java.util.Set<String> PDF_TYPES = java.util.Set.of(
            "application/pdf"
    );

    public String store(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        String subDir      = resolveSubDir(contentType);

        Path dirPath = Paths.get(uploadDir, subDir);
        Files.createDirectories(dirPath);

        String ext      = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + ext;
        Path   target   = dirPath.resolve(filename);

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        // URL publique retournée au frontend
        return "/uploads/" + subDir + "/" + filename;
    }

    private String resolveSubDir(String contentType) {
        if (contentType == null)             return "misc";
        if (IMAGE_TYPES.contains(contentType)) return "images";
        if (VIDEO_TYPES.contains(contentType)) return "videos";
        if (AUDIO_TYPES.contains(contentType)) return "audios";
        if (PDF_TYPES.contains(contentType))   return "pdfs";
        return "misc";
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }
}