package com.drive.flashbox.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.stereotype.Service;

import com.drive.flashbox.dto.request.BoxRequest;
import com.drive.flashbox.dto.response.BoxResponse;
import com.drive.flashbox.entity.Box;
import com.drive.flashbox.entity.BoxUser;
import com.drive.flashbox.entity.Picture;
import com.drive.flashbox.entity.User;
import com.drive.flashbox.entity.enums.RoleType;
import com.drive.flashbox.repository.BoxRepository;
import com.drive.flashbox.repository.PictureRepository;
import com.drive.flashbox.repository.UserRepository;
import com.drive.flashbox.repository.BoxUserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class BoxService {
	final private BoxRepository boxRepository;
	final private BoxUserRepository boxUserRepository;
	final private UserRepository userRepository;
	private final PictureRepository pictureRepository;
	private final S3Service s3Service;

	@Transactional
	public String generateZipAndGetPresignedUrl(Long bid, Long uid) {
		// 1) 권한 체크 (예: 박스 소유자 또는 멤버 여부)
		User user = userRepository.findById(uid)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. uid=" + uid));
		Box box = boxRepository.findById(bid)
				.orElseThrow(() -> new IllegalArgumentException("Box를 찾을 수 없습니다. bid=" + bid));
		// 실제 권한 체크 로직은 필요에 따라 추가하세요.

		// 2) 박스 내 사진 조회
		List<Picture> pictures = pictureRepository.findAllByBoxBid(bid);
		if (pictures.isEmpty()) {
			throw new IllegalArgumentException("박스에 사진이 없습니다.");
		}

		// 3) ZIP 파일 생성 (메모리 내 생성; 대용량일 경우 다른 방식을 고려)
		byte[] zipBytes = createZipFileInMemory(pictures);

		// 4) ZIP 파일을 S3에 업로드 (예: "temp/박스이름.zip" 경로)
//		String safeBoxName = box.getName().trim().replaceAll("\\s+", "_");
		String zipFileName = "temp/" + box.getBid() + ".zip";

		try {
			s3Service.uploadFileToS3(zipFileName, zipBytes);
		} catch (Exception e) {
			throw new IllegalStateException("ZIP 파일 업로드 중 오류가 발생했습니다. zipFileName=" + zipFileName, e);
		}

		// 5) URL 반환
		return s3Service.generatePresignedUrl(zipFileName);
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
			throw new IllegalStateException("ZIP 파일 생성 중 오류가 발생했습니다.", e);
		}
	}
	
	@Transactional
	public Box createBox(BoxRequest boxDto) {
		// 유저가 없으면 생성이 안되서 임의로 1번 유저가 생성했다고 가정
		User user = userRepository.getReferenceById(1L);
		
		Box box = BoxRequest.toEntity(boxDto, user);
		
		// BoxUser에 생성한 유저와 OWNER role 등록하는 메서드
		box.addBoxUser(user, RoleType.OWNER);
		
		Box newBox = boxRepository.save(box);
		
		// box 생성 후에 s3 폴더 생성해야 id 값 정상적으로 입력됨
		s3Service.createS3Folder(newBox.getBid());

		return newBox;
	}
	
    // 박스 전체 조회
    public List<Box> getAllBoxes() {
        return boxRepository.findAll();
    }
	
	public BoxResponse getBox(Long bid) {
		return boxRepository.findById(bid).map(BoxResponse::from).orElseThrow(() -> new IllegalArgumentException("Box를 찾을 수 없습니다."));
	}
	
	@Transactional
	public void updateBox(Long bid, BoxRequest boxDto) {
		Box box = boxRepository.findById(bid).orElseThrow(() -> new IllegalArgumentException("Box를 찾을 수 없습니다."));
		box.editBox(boxDto.getName(),
					boxDto.getEventStartDate().atStartOfDay(),
					boxDto.getEventEndDate().atStartOfDay().plusDays(1).minusSeconds(1));	
	}
 
	public void inviteUserToBox(Long boxId, Long userId) {
		// 1. 박스와 유저를 조회
		Box box = boxRepository.findById(boxId)
	              .orElseThrow(() -> new NoSuchElementException("해당 박스를 찾을 수 없습니다. ID: " + boxId));
	      User user = userRepository.findById(userId)
	              .orElseThrow(() -> new NoSuchElementException("해당 유저를 찾을 수 없습니다. ID: " + userId));
	
	      // 2. 이미 초대된 유저인지 검사 (옵션)
	      boolean alreadyMember = boxUserRepository.existsByBoxAndUser(box, user);
	      if (alreadyMember) {
	          throw new IllegalStateException("이미 초대된 유저입니다.");
	      }
	
	      // 3. 중간 엔티티(예: BoxUser) 생성
	      BoxUser boxUser = new BoxUser();
	      boxUser.setBox(box);
	      boxUser.setUser(user);
	      boxUser.setRole(RoleType.MEMBER); // 예: MEMBER / OWNER
	
	      // 4. DB에 저장
	      boxUserRepository.save(boxUser);
	}

  	@Transactional
	public void deleteBox(Long bid) {
  		Box box = boxRepository.findById(bid).orElseThrow(() -> new IllegalStateException("Box를 찾을 수 없습니다."));
		boxRepository.deleteById(bid);
		s3Service.deleteS3Folder(box.getBid());
	}
}
