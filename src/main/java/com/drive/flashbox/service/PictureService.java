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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    public List<String> generateDownloadUrls(Long bid, List<Long> pids) {
        // 1) Box가 존재하는지 확인 (권한 체크가 필요하다면 여기서 추가)
        Box box = boxRepository.findById(bid)
                .orElseThrow(() -> new IllegalArgumentException("Box를 찾을 수 없습니다."));

        // 2) (Optional) 사용자 권한 체크 - 필요시 구현
        // 예: User user = userRepository.findById(1L).orElseThrow(...);

        List<String> downloadUrls = new ArrayList<>();

        for (Long pid : pids) {
            // 3) Picture 조회
            Picture picture = pictureRepository.findByPidAndBoxBid(pid, bid)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이미지입니다. pid=" + pid));

            // 4) S3에 저장된 객체 키 (현재 picture.getImageUrl()가 전체 URL인지, 키인지 확인)
            // 만약 DB에 전체 URL을 저장하고 있다면, 곧바로 사용 가능
            // 여기서는 s3Key = picture.getImageUrl() 라고 가정
            String s3Key = picture.getImageUrl();

            // 5) Pre-signed URL 생성
            String presignedUrl = s3Service.generatePresignedUrl(s3Key);

            downloadUrls.add(presignedUrl);
        }

        return downloadUrls;
    }

    
    @Transactional
    public void deletePicture(Long bid, Long pid) {
        Picture picture = pictureRepository.findByPidAndBoxBid(pid, bid)
                .orElseThrow(() -> new IllegalArgumentException("해당 이미지 또는 박스를 찾을 수 없습니다."));
        
        pictureRepository.delete(picture);
    }
    
}