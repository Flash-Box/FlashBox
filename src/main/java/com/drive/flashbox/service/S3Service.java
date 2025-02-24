package com.drive.flashbox.service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

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
            throw new IllegalStateException("파일 업로드 중 오류가 발생했습니다.", e);
        } catch (AmazonClientException e) {
            throw new IllegalStateException("Error communicating with S3", e);
        }
    }

    // S3 객체의 전체 URL 반환
    public String getFileUrl(String s3Key) {
        try {
            return amazonS3.getUrl(bucketName, s3Key).toString();
        } catch (Exception e) {
            throw new IllegalStateException("파일 URL 생성 중 오류가 발생했습니다.", e);
        }
    }

    // S3에 있는 객체를 바이트 배열로 다운로드 (전체 URL인 경우 객체 키만 추출)
    public byte[] downloadFileAsBytes(String fileUrl) {
        // fileKey가 전체 URL일 경우, 객체 키만 추출
        String key = extractKeyFromUrl(fileUrl);
        S3Object s3Object = amazonS3.getObject(bucketName, key);
        try (S3ObjectInputStream s3is = s3Object.getObjectContent()) {
            return s3is.readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException("S3 파일 다운로드 중 오류가 발생했습니다.", e);
        } catch (Exception e) {
            throw new IllegalStateException("S3 파일 다운로드 중 예상치 못한 오류가 발생했습니다. fileUrl=" + fileUrl, e);
        }
    }

    // 업로드된 파일에 대한 Pre-signed URL 생성
    public String generatePresignedUrl(String fileUrl) {
        String key = extractKeyFromUrl(fileUrl);
        // 유효 기간 설정 (예: 1시간)
        Date expiration = new Date(System.currentTimeMillis() + 1000 * 60 * 60);
        try{
            GeneratePresignedUrlRequest generatePresignedUrlRequest =
                    new GeneratePresignedUrlRequest(bucketName, key)
                            .withMethod(HttpMethod.GET)
                            .withExpiration(expiration);
            return amazonS3.generatePresignedUrl(generatePresignedUrlRequest).toString();
        } catch (Exception e) {
            throw new IllegalStateException("Pre-signed URL 생성 중 오류가 발생했습니다." , e);
        }
    }

    // 전체 URL에서 객체 키만 추출 (예: "https://t5-flashbox.s3.ap-northeast-2.amazonaws.com/box-1/3.png" -> "box-1/3.png")
    public String extractKeyFromUrl(String fileUrl) {
        int index = fileUrl.indexOf("amazonaws.com/");
        if (index != -1) {
            return fileUrl.substring(index + "amazonaws.com/".length());
        }
        return fileUrl;
    }
    
    // 박스 생성 시 S3에 폴더 생성하는 메서드
    public void createS3Folder(Long boxId) {
        String folderPath = boxId + "/";

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(0);

        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(new byte[0])) {
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucketName, folderPath, byteArrayInputStream, metadata);
            amazonS3.putObject(putObjectRequest);
        } catch (IOException e) {
            throw new IllegalStateException("S3 폴더 생성 중 오류가 발생했습니다.", e);
        }
    }

    public void deleteS3Folder(Long boxId) {
        String folderPath = boxId + "/";

        // S3에서 해당 폴더 내의 객체 목록을 가져옴
        ListObjectsV2Request listObjectsRequest = new ListObjectsV2Request()
                .withBucketName(bucketName)
                .withPrefix(folderPath);  // "/" 구분자 제거 (하위 객체 포함 검색)

        ListObjectsV2Result result = amazonS3.listObjectsV2(listObjectsRequest);
        List<S3ObjectSummary> objects = result.getObjectSummaries();

        // 폴더 내 객체 삭제
        for (S3ObjectSummary objectSummary : objects) {
            amazonS3.deleteObject(bucketName, objectSummary.getKey());
        }

        // 🔹 box 폴더 삭제 가능 여부 체크
        ListObjectsV2Request boxFolderRequest = new ListObjectsV2Request()
                .withBucketName(bucketName)
                .withPrefix("");  // 모든 객체 확인

        ListObjectsV2Result boxFolderResult = amazonS3.listObjectsV2(boxFolderRequest);

        // box 폴더에 남아있는 객체가 없으면 삭제
        if (boxFolderResult.getObjectSummaries().isEmpty() && boxFolderResult.getCommonPrefixes().isEmpty()) {
            amazonS3.deleteObject(bucketName, folderPath);
        }
    }
}