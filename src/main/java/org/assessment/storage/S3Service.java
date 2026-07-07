package org.assessment.storage;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {

    String uploadFile(MultipartFile file);

    String uploadFile(MultipartFile file, String folder);

    void deleteFile(String fileKey);

    String getFileUrl(String fileKey);
    String generatePresignedUrl(String fileUrlOrKey);
}