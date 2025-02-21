package com.drive.flashbox.service;

import com.drive.flashbox.entity.Box;
import com.drive.flashbox.entity.Picture;
import com.drive.flashbox.entity.User;
import com.drive.flashbox.repository.BoxRepository;
import com.drive.flashbox.repository.PictureRepository;
import com.drive.flashbox.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RequiredArgsConstructor
@Service
public class BoxService {
	final private BoxRepository boxRepository;
	final private UserRepository userRepository;
	private final PictureRepository pictureRepository;
	private final S3Service s3Service;

	@Transactional
	public String generateZipAndGetPresignedUrl(Long bid, Long uid) {
		// 1) 권한 체크 (예: 박스 소유자 또는 멤버 여부)
		User user = userRepository.findById(uid)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
		Box box = boxRepository.findById(bid)
				.orElseThrow(() -> new IllegalArgumentException("Box를 찾을 수 없습니다."));
		// 실제 권한 체크 로직은 필요에 따라 추가하세요.

		// 2) 박스 내 사진 조회
		List<Picture> pictures = pictureRepository.findAllByBoxBid(bid);
		if (pictures.isEmpty()) {
			throw new IllegalArgumentException("박스에 사진이 없습니다.");
		}

		// 3) ZIP 파일 생성 (메모리 내 생성; 대용량일 경우 다른 방식을 고려)
		byte[] zipBytes = createZipFileInMemory(pictures);

		// 4) ZIP 파일을 S3에 업로드 (예: "temp/box_{bid}.zip" 경로)
		String zipFileName = "temp/box_" + bid + ".zip";
		s3Service.uploadFileToS3(zipFileName, zipBytes);

		// 5) 업로드된 ZIP 파일의 Pre-signed URL 생성
		String presignedUrl = s3Service.generatePresignedUrl(zipFileName);

		// 6) URL 반환
		return presignedUrl;
	}

	private byte[] createZipFileInMemory(List<Picture> pictures) {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
			 ZipOutputStream zos = new ZipOutputStream(bos)) {

			// 중복 파일명을 체크하기 위한 Set
			Set<String> fileNameSet = new HashSet<>();

			for (Picture picture : pictures) {
				String fileName = picture.getName();
				// 유일한 파일명 생성
				String uniqueFileName = fileName;
				int count = 1;
				while (fileNameSet.contains(uniqueFileName)) {
					int dotIndex = fileName.lastIndexOf('.');
					if (dotIndex != -1) {
						uniqueFileName = fileName.substring(0, dotIndex) + "(" + count + ")" + fileName.substring(dotIndex);
					} else {
						uniqueFileName = fileName + "(" + count + ")";
					}
					count++;
				}
				fileNameSet.add(uniqueFileName);

				// S3에서 파일 바이트 배열 가져오기
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


}
