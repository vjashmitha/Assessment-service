package org.assessment.storage;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

import org.assessment.exception.FileUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

    @Override
    public String uploadFile(MultipartFile file) {
        return uploadFile(file, "");
    }

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String fileName = UUID.randomUUID().toString() + fileExtension;
        String key = (folder == null || folder.isBlank()) ? fileName : folder + "/" + fileName;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            return getFileUrl(key);
        } catch (IOException e) {
            log.error("Failed to upload file to S3", e);
            throw new FileUploadException("Failed to upload file to S3: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileKey) {
        if (fileKey == null || fileKey.isBlank()) {
            return;
        }

        String key = fileKey;
        String s3Prefix = String.format("https://%s.s3.%s.amazonaws.com/", bucketName, region);
        if (key.startsWith(s3Prefix)) {
            key = key.substring(s3Prefix.length());
        }

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            log.error("Failed to delete file from S3: {}", key, e);
        }
    }

    @Override
    public String getFileUrl(String fileKey) {
        if (fileKey == null || fileKey.isBlank()) {
            return null;
        }
        if (fileKey.startsWith("http://") || fileKey.startsWith("https://")) {
            return fileKey;
        }
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileKey);
    }
    
    @Override
    public String generatePresignedUrl(String fileUrlOrKey) {
        if (fileUrlOrKey == null || fileUrlOrKey.isBlank()) {
            return null;
        }

        String key = extractKey(fileUrlOrKey);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(30))
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest)
                .url()
                .toString();
    }

    private String extractKey(String fileUrlOrKey) {
        String s3Prefix = String.format(
                "https://%s.s3.%s.amazonaws.com/",
                bucketName,
                region
        );

        if (fileUrlOrKey.startsWith(s3Prefix)) {
            return fileUrlOrKey.substring(s3Prefix.length());
        }

        return fileUrlOrKey;
    }
    
    
}