package com.drive.flashbox.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName; // 실제 버킷명

    // 파일을 바이트 배열로 받아 S3에 업로드
    public void uploadFileToS3(String s3Key, byte[] fileBytes) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(fileBytes.length);

        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fileBytes)) {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, s3Key, byteArrayInputStream, metadata);
            amazonS3.putObject(putObjectRequest);
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
        }
    }

    // S3 객체의 전체 URL 반환
    public String getFileUrl(String s3Key) {
        return amazonS3.getUrl(bucketName, s3Key).toString();
    }

    // S3에 있는 객체를 바이트 배열로 다운로드 (전체 URL인 경우 객체 키만 추출)
    public byte[] downloadFileAsBytes(String fileKey) {
        // fileKey가 전체 URL일 경우, 객체 키만 추출
        String key = extractKey(fileKey);
        S3Object s3Object = amazonS3.getObject(bucketName, key);
        try (S3ObjectInputStream s3is = s3Object.getObjectContent()) {
            return s3is.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("S3 파일 다운로드 중 오류가 발생했습니다.", e);
        }
    }

    // 업로드된 파일에 대한 Pre-signed URL 생성
    public String generatePresignedUrl(String s3Key) {
        // 유효 기간 설정 (예: 1시간)
        Date expiration = new Date(System.currentTimeMillis() + 1000 * 60 * 60);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, s3Key)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);

        return amazonS3.generatePresignedUrl(generatePresignedUrlRequest).toString();
    }

    // 전체 URL에서 S3 객체 키만 추출하는 헬퍼 메서드
    private String extractKey(String fileUrl) {
        if (fileUrl.startsWith("http")) {
            // 예: "https://t5-flashbox.s3.ap-northeast-2.amazonaws.com/temp/box_1/file.jpg"
            // "amazonaws.com/" 이후 부분을 객체 키로 사용
            int index = fileUrl.indexOf("amazonaws.com/");
            if (index != -1) {
                return fileUrl.substring(index + "amazonaws.com/".length());
            }
        }
        // 이미 객체 키인 경우 그대로 반환
        return fileUrl;
    }
}