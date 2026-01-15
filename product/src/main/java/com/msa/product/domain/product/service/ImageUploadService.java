package com.msa.product.domain.product.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
public class ImageUploadService {

    @Value("${file.upload-dir:uploads/images}")
    private String uploadDir;

    @Value("${server.port:8080}")
    private String serverPort;

    private boolean usesTempDirectory = false;

    public String saveImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 비어있습니다.");
        }

        // 현재 작업 디렉토리 기준으로 절대 경로 생성
        String currentDir = System.getProperty("user.dir");
        Path uploadPath = Paths.get(currentDir, uploadDir);

        log.info("업로드 디렉토리 경로: {}", uploadPath.toAbsolutePath());

        // 업로드 디렉토리 생성
        boolean useTempDir = false;
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
                log.info("업로드 디렉토리 생성 완료: {}", uploadPath.toAbsolutePath());
            } catch (IOException e) {
                log.error("업로드 디렉토리 생성 실패: {}. 임시 디렉토리 사용", uploadPath.toAbsolutePath(), e);
                // 임시 디렉토리 사용
                String tempDir = System.getProperty("java.io.tmpdir");
                uploadPath = Paths.get(tempDir, "product-images");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                useTempDir = true;
                log.info("임시 디렉토리 사용: {}", uploadPath.toAbsolutePath());
            }
        }

        // 파일명 생성 (UUID + 원본 파일명)
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString() + extension;

        // 파일 저장
        Path filePath = uploadPath.resolve(filename);
        file.transferTo(filePath.toFile());

        log.info("이미지 저장 완료: {}", filePath.toString());

        // HTTP URL 형태로 반환
        String baseUrl = "http://localhost:" + serverPort;
        String imageUrl;
        if (useTempDir) {
            imageUrl = baseUrl + "/temp-images/" + filename;
        } else {
            imageUrl = baseUrl + "/images/" + filename;
        }

        log.info("이미지 URL: {}", imageUrl);
        return imageUrl;
    }

    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        try {
            Path filePath = Paths.get(imageUrl);
            Files.deleteIfExists(filePath);
            log.info("이미지 삭제 완료: {}", imageUrl);
        } catch (IOException e) {
            log.error("이미지 삭제 실패: {}", imageUrl, e);
        }
    }
}
