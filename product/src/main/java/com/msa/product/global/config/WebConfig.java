package com.msa.product.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads/images}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 현재 작업 디렉토리 기준 경로
        String currentDir = System.getProperty("user.dir");
        Path uploadPath = Paths.get(currentDir, uploadDir);
        File uploadFile = uploadPath.toFile();

        String uploadLocation = "file:" + uploadPath.toAbsolutePath().toString() + File.separator;

        log.info("=== 정적 리소스 핸들러 등록 ===");
        log.info("현재 작업 디렉토리: {}", currentDir);
        log.info("업로드 설정 경로: {}", uploadDir);
        log.info("절대 경로: {}", uploadPath.toAbsolutePath());
        log.info("디렉토리 존재 여부: {}", uploadFile.exists());
        log.info("리소스 위치: {}", uploadLocation);
        log.info("URL 패턴: /images/**");

        // /images/** URL을 실제 파일 시스템 경로로 매핑
        registry.addResourceHandler("/images/**")
                .addResourceLocations(uploadLocation);

        // 임시 디렉토리 매핑 (폴백용)
        String tempDir = System.getProperty("java.io.tmpdir");
        Path tempUploadPath = Paths.get(tempDir, "product-images");
        File tempFile = tempUploadPath.toFile();

        String tempLocation = "file:" + tempUploadPath.toAbsolutePath().toString() + File.separator;

        log.info("=== 임시 리소스 핸들러 등록 ===");
        log.info("임시 디렉토리: {}", tempDir);
        log.info("임시 업로드 절대 경로: {}", tempUploadPath.toAbsolutePath());
        log.info("임시 디렉토리 존재 여부: {}", tempFile.exists());
        log.info("임시 리소스 위치: {}", tempLocation);
        log.info("URL 패턴: /temp-images/**");

        registry.addResourceHandler("/temp-images/**")
                .addResourceLocations(tempLocation);
    }
}