package com.drive.flashbox.service;

import com.drive.flashbox.dto.PictureDto;
import com.drive.flashbox.entity.Box;
import com.drive.flashbox.entity.Picture;
import com.drive.flashbox.entity.User;
import com.drive.flashbox.repository.BoxRepository;
import com.drive.flashbox.repository.PictureRepository;
import com.drive.flashbox.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class PictureService {

    private final PictureRepository pictureRepository;
    private final BoxRepository boxRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Transactional(readOnly = true)
    public PictureDto getPictureDetails(Long bid, Long pid) {
        Picture picture = pictureRepository.findByPidAndBoxBid(pid, bid)
                .orElseThrow(() -> new IllegalArgumentException("해당 이미지 또는 박스를 찾을 수 없습니다."));
        
        return PictureDto.builder()
                .pid(picture.getPid())
                .name(picture.getName())
                .uploadDate(picture.getUploadDate())
                .imageUrl(picture.getImageUrl())
                .userId(picture.getUser().getId())
                .boxId(picture.getBox().getBid())
                .build();
    }

    // 이미지 업로드
    @Transactional
    public List<Long> uploadPictures(Long bid, MultipartFile[] files) {
        // 1. 박스 조회 (존재하지 않으면 예외 처리)
        Box box = boxRepository.findById(bid)
                .orElseThrow(() -> new IllegalArgumentException("Box를 찾을 수 없습니다."));

        // (필요하다면) 인증된 사용자 정보도 가져오기
        // 예시에서는 임의로 1번 사용자를 사용합니다.
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<Long> pictureIds = new ArrayList<>();

        for (MultipartFile file : files) {
            // 2. 파일 형식 검증 (예: 이미지 파일인지 확인)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("지원하지 않는 파일 형식입니다.");
            }

            try {
                // 3. S3에 파일 업로드
                // 파일 업로드 경로 예: "{boxName}/{originalFilename}"
                String safeBoxName = box.getName().trim().replaceAll("\\s+", "_");
                String s3Key = safeBoxName + "/" + file.getOriginalFilename();
                byte[] fileBytes = file.getBytes();
                s3Service.uploadFileToS3(s3Key, fileBytes);

                // 4. 전체 URL 가져오기
                String fileUrl = s3Service.getFileUrl(s3Key);

                // 5. Picture 엔티티 생성 및 DB 저장
                Picture picture = Picture.builder()
                        .name(file.getOriginalFilename())
                        .uploadDate(LocalDateTime.now())
                        .imageUrl(fileUrl) // 전체 URL 저장
                        .user(user)
                        .box(box)
                        .build();
                Picture savedPicture = pictureRepository.save(picture);
                pictureIds.add(savedPicture.getPid());
            } catch (Exception e) {
                throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
            }
        }

        return pictureIds;
    }

    // 이미지 다운로드
    @Transactional(readOnly = true)
    public String generateDownloadUrlForPictures(Long bid, List<Long> pids) {
        if (pids.size() == 1) {
            // 단일 이미지인 경우
            Picture picture = pictureRepository.findByPidAndBoxBid(pids.get(0), bid)
                    .orElseThrow(() -> new IllegalArgumentException("해당 이미지 또는 박스를 찾을 수 없습니다. pid=" + pids.get(0)));
            // DB에 저장된 imageUrl은 전체 URL이라 가정 (이전 방식 B)
            return s3Service.generatePresignedUrl(picture.getImageUrl());
        } else {
            // 여러 이미지인 경우 ZIP 파일로 묶기
            byte[] zipBytes = createZipFileForPictures(bid, pids);

            // 박스 정보를 조회해서 파일 이름에 박스 이름 사용 (안전한 형태로 변환)
            Box box = boxRepository.findById(bid)
                    .orElseThrow(() -> new IllegalArgumentException("Box를 찾을 수 없습니다."));
            String safeBoxName = box.getName().trim().replaceAll("\\s+", "_");
            String zipFileName = "temp/" + safeBoxName + ".zip";

            s3Service.uploadFileToS3(zipFileName, zipBytes);
            return s3Service.generatePresignedUrl(zipFileName);
        }
    }

    // 여러 이미지를 zip 파일로 만들기 위해 List로 변환
    @Transactional(readOnly = true)
    public byte[] createZipFileForPictures(Long bid, List<Long> pids) {
        List<Picture> pictures = new ArrayList<>();
        for (Long pid : pids) {
            Picture picture = pictureRepository.findByPidAndBoxBid(pid, bid)
                    .orElseThrow(() -> new IllegalArgumentException("해당 이미지 또는 박스를 찾을 수 없습니다. pid=" + pid));
            pictures.add(picture);
        }
        return createZipFileInMemory(pictures);
    }

    // zip 파일 생성
    private byte[] createZipFileInMemory(List<Picture> pictures) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(bos)) {

            // 중복 파일명 방지를 위한 Set
            Set<String> fileNameSet = new HashSet<>();
            for (Picture picture : pictures) {
                String originalFileName = picture.getName();
                String uniqueFileName = originalFileName;
                int count = 1;
                while (fileNameSet.contains(uniqueFileName)) {
                    int dotIndex = originalFileName.lastIndexOf('.');
                    if (dotIndex != -1) {
                        uniqueFileName = originalFileName.substring(0, dotIndex) + "(" + count + ")" + originalFileName.substring(dotIndex);
                    } else {
                        uniqueFileName = originalFileName + "(" + count + ")";
                    }
                    count++;
                }
                fileNameSet.add(uniqueFileName);

                // picture.getImageUrl()에는 전체 URL이 저장
                // S3Service 내의 extractKeyFromUrl()를 이용해 객체 키를 추출
                String s3Key = s3Service.extractKeyFromUrl(picture.getImageUrl());
                byte[] fileBytes = s3Service.downloadFileAsBytes(picture.getImageUrl());
                ZipEntry entry = new ZipEntry(uniqueFileName);
                zos.putNextEntry(entry);
                zos.write(fileBytes);
                zos.closeEntry();
            }
            zos.finish();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("ZIP 파일 생성 중 오류가 발생했습니다.", e);
        }
    }

    
    @Transactional
    public void deletePicture(Long bid, Long pid) {
        Picture picture = pictureRepository.findByPidAndBoxBid(pid, bid)
                .orElseThrow(() -> new IllegalArgumentException("해당 이미지 또는 박스를 찾을 수 없습니다."));
        
        pictureRepository.delete(picture);
    }
    
}