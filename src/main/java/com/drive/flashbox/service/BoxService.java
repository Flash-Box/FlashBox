package com.drive.flashbox.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.drive.flashbox.dto.response.BoxCreateResponse;

import org.springframework.stereotype.Service;

import com.drive.flashbox.dto.request.BoxRequest;
import com.drive.flashbox.dto.response.BoxResponse;
import com.drive.flashbox.dto.response.BoxUserResponse;
import com.drive.flashbox.entity.Box;
import com.drive.flashbox.entity.BoxUser;
import com.drive.flashbox.entity.Picture;
import com.drive.flashbox.entity.User;
import com.drive.flashbox.entity.enums.RoleType;
import com.drive.flashbox.repository.BoxRepository;
import com.drive.flashbox.repository.BoxUserRepository;
import com.drive.flashbox.repository.PictureRepository;
import com.drive.flashbox.repository.UserRepository;

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
	private final Map<Long, ScheduledTaskInfo> scheduledTasks = new HashMap<>();
	private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    {
        scheduler.initialize(); // 🔹 필드 초기화와 동시에 스케줄러 설정
    }

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
			throw new IllegalStateException("박스에 사진이 없습니다.");
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
	public BoxCreateResponse createBox(BoxRequest boxDto,Long userId) {

		User user = userRepository.getReferenceById(userId); 
		Box box = BoxRequest.toEntity(boxDto, user);
		
		// BoxUser에 생성한 유저와 OWNER role 등록하는 메서드
		box.addBoxUser(user, RoleType.OWNER);
		
		Box newBox = boxRepository.save(box);
		
		// box 생성 후에 s3 폴더 생성해야 id 값 정상적으로 입력됨
		s3Service.createS3Folder(newBox.getBid());

		BoxCreateResponse boxCreateResponse = BoxCreateResponse.of(box, user);
		System.out.println(boxCreateResponse);

		return boxCreateResponse;
	}
	
    // 박스 전체 조회
	public List<BoxResponse> getAllUserBoxes(Long uid) {
		List<Long> bids = boxUserRepository.findAllByUserId(uid).stream().map(boxuser -> boxuser.getBox().getBid()).toList();
		List<Box> boxes = boxRepository.findAllById(bids);

	    return boxes.stream().map(box -> {
	        List<BoxUserResponse> members = boxUserRepository.findAllByBoxBid(box.getBid()).stream()
	                .map(BoxUserResponse::from)
	                .collect(Collectors.toList());

	        List<String> images = pictureRepository.findAllByBoxBid(box.getBid()).stream()
	                .map(Picture::getImageUrl)
	                .collect(Collectors.toList());

	        return BoxResponse.from(box, members, images); // ✅ members 포함하여 올바르게 호출
	    }).collect(Collectors.toList());
	}

	// SCRUM-69-activate-search-bar : 검색 기능 추가
    public List<BoxResponse> searchBoxesByKeyword(String keyword, Long uid) {
        List<Long> bids = boxUserRepository.findAllByUserId(uid).stream().map(boxuser -> boxuser.getBox().getBid()).toList();
        List<Box> boxes = boxRepository.findAllById(bids);

        return boxes.stream()
                .filter(box -> box.getName().toLowerCase().contains(keyword.toLowerCase())) // 이름으로 필터링 
                .map(box -> {
                    List<BoxUserResponse> members = boxUserRepository.findAllByBoxBid(box.getBid()).stream()
                            .map(BoxUserResponse::from)
                            .collect(Collectors.toList());
                    List<String> images = pictureRepository.findAllByBoxBid(box.getBid()).stream()
                            .map(Picture::getImageUrl)
                            .collect(Collectors.toList());
                    return BoxResponse.from(box, members, images);
                })
                .collect(Collectors.toList());
    }	
	
    // 박스 조회, 수정 --------------------------------------------------------------------- SCRUM-30-view-members
	public BoxResponse getBox(Long bid) {				
	    Box box = boxRepository.findById(bid)		
	            .orElseThrow(() -> new IllegalArgumentException("Box를 찾을 수 없습니다.")); 

	    List<BoxUserResponse> members = boxUserRepository.findAllByBoxBid(bid).stream()
	            .map(BoxUserResponse::from)
	            .collect(Collectors.toList());

	    return BoxResponse.from(box, members, Collections.emptyList());  // ✅ 빈 리스트 전달하여 오류 해결!
	}
	
	@Transactional
	public BoxResponse updateBox(Long bid, Long uid, BoxRequest boxDto) {
	    Box box = boxRepository.findById(bid)
	        .orElseThrow(() -> new IllegalArgumentException("Box를 찾을 수 없습니다."));

	    BoxUser boxUser = boxUserRepository.findByBox_BidAndUser_Id(bid, uid)
	        .orElseThrow(() -> new IllegalStateException("해당 박스에 참여하지 않았습니다."));

	    if (boxUser.getRole() != RoleType.OWNER && boxUser.getRole() != RoleType.MEMBER) {
	        throw new IllegalStateException("박스의 소유자 또는 멤버만 수정할 수 있습니다.");
	    }
	    
		box.editBox(boxDto.getName(),
					boxDto.getEventStartDate().atStartOfDay(),
					boxDto.getEventEndDate().atStartOfDay().plusDays(1).minusSeconds(1));

		return BoxResponse.from(box);

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
	      boxUser.setParticipateDate(LocalDateTime.now()); // participateDate 추가 ✅
	      boxUser.setRole(RoleType.MEMBER); // 예: MEMBER / OWNER
	
	      // 4. DB에 저장
	      boxUserRepository.save(boxUser);
	}
	
	@Transactional
	public void deleteBoxes(List<Long> bidList, Long uid) {
	    for (Long bid : bidList) {
	        deleteBox(bid, uid);
	    }
	}

  	@Transactional
	public void deleteBox(Long bid, Long uid) {
  			Box box = boxRepository.findById(bid)
  		        .orElseThrow(() -> new IllegalStateException("Box를 찾을 수 없습니다."));

  		    BoxUser boxUser = boxUserRepository.findByBox_BidAndUser_Id(bid, uid)
  		        .orElseThrow(() -> new IllegalStateException("해당 박스에 참여하지 않았습니다."));

  		    if (boxUser.getRole() != RoleType.OWNER) {
  		        throw new IllegalStateException("박스의 소유자만 삭제할 수 있습니다.");
  		    }
		boxRepository.deleteById(bid);
		s3Service.deleteS3Folder(box.getBid());
	}
  	
  	// 6시간마다 실행 -> boomDate가 하루 남은 박스를 찾아 삭제 예약
    @Scheduled(fixedRate = 6 * 60 * 60 * 1000)
//  	@Scheduled(fixedRate = 60000) // 테스트를 위해 1분마다 실행
    public void scheduleExpirationTasks() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneDayLater = now.plusDays(1); // 하루 뒤
        
        // 현재로부터 24시간 이내에 폭파하는 박스 조회
        List<Box> nearExpiredBoxes = boxRepository.findAllByBoomDateBetween(now, oneDayLater);
        
        // 이미 기한이 지난 박스들은 삭제
        List<Box> expiredBoxes = boxRepository.findAllByBoomDateBefore(now);
        for (Box box : expiredBoxes) {
            deleteBoxAndS3Folder(box); // 즉시 삭제
        }

        System.out.println("예약 작업 실행: " + LocalDateTime.now());
        System.out.println("하루 남은 박스 개수: " + nearExpiredBoxes.size());

        for (Box box : nearExpiredBoxes) {
            scheduleDeletionTask(box);
        }
        
        printScheduledTasks();
    }

    // boomDate에 맞춰 자동 삭제 예약
  	public void scheduleDeletionTask(Box box) {
        LocalDateTime boomDate = box.getBoomDate();
        long delay = Duration.between(LocalDateTime.now(), boomDate).toMillis();

        // 이미 예약된 작업이 있으면 중복 예약 방지
        if (scheduledTasks.containsKey(box.getBid())) {
            System.out.println("박스 ID: " + box.getBid() + " 이미 예약된 작업이 있습니다.");
            return;
        }

        if (delay > 0) { // 과거 시간이 아닌 경우에만 예약
            ScheduledFuture<?> scheduledTask = scheduler.schedule(
                    () -> deleteBoxAndS3Folder(box),
                    Instant.now().plusMillis(delay)
            );

            // 예약된 작업을 map에 저장 (ScheduledTaskInfo 객체로 저장)
            scheduledTasks.put(box.getBid(), new ScheduledTaskInfo(scheduledTask, boomDate));

            System.out.println("박스 ID: " + box.getBid() +
                    " | 삭제 예약 시간: " + boomDate);
        }
    }

    // 박스 + S3 폴더 삭제
    private void deleteBoxAndS3Folder(Box box) {
        System.out.println("박스 ID: " + box.getBid() + " 삭제됨 (BoomDate: " + box.getBoomDate() + ")");

        boxRepository.deleteById(box.getBid());
        s3Service.deleteS3Folder(box.getBid());
    }
    
    // boomDate를 예약 상태를 다시 확인하는 메서드
    public void handleBoomDateChange(Box box) {
        LocalDateTime boomDate = box.getBoomDate();
        long delay = Duration.between(LocalDateTime.now(), boomDate).toMillis();

        // 예약된 작업 목록에 있고, boomDate가 변경되었으면 기존 예약 취소
        if (scheduledTasks.containsKey(box.getBid())) {
            ScheduledTaskInfo existingTaskInfo = scheduledTasks.get(box.getBid());
            LocalDateTime existingBoomDate = existingTaskInfo.getBoomDate();
            ScheduledFuture<?> existingTask = existingTaskInfo.getScheduledTask();

            if (!existingBoomDate.isEqual(boomDate)) {
                System.out.println("박스 ID: " + box.getBid() + " 의 BoomDate 변경 - 기존 예약 취소");
                existingTask.cancel(true); // 기존 예약 취소
                scheduledTasks.remove(box.getBid()); // 예약 목록에서 제거
            }
        }

        // delay가 0보다 크고, 하루 이내인 경우에만 예약
        if (delay > 0 && delay <= 24 * 60 * 60 * 1000) { // 하루 이내
            ScheduledFuture<?> scheduledTask = scheduler.schedule(
                    () -> deleteBoxAndS3Folder(box),
                    Instant.now().plusMillis(delay) // 예약 시간 설정
            );

            // 예약된 작업을 map에 저장
            scheduledTasks.put(box.getBid(), new ScheduledTaskInfo(scheduledTask, boomDate));

            System.out.println("박스 ID: " + box.getBid() +
                    " | 삭제 예약 시간: " + boomDate);
        } else {
            System.out.println("박스 ID: " + box.getBid() + " | BoomDate가 연장되어 예약되지 않았습니다.");
        }
    }
    
    @Transactional
    public void extendBoomDate(Long bid, Long uid) {
        Box box = boxRepository.findById(bid)
                .orElseThrow(() -> new IllegalArgumentException("Box를 찾을 수 없습니다."));

        BoxUser boxUser = boxUserRepository.findByBox_BidAndUser_Id(bid, uid)
                .orElseThrow(() -> new IllegalStateException("해당 박스에 참여하지 않았습니다."));

        if (boxUser.getRole() != RoleType.OWNER && boxUser.getRole() != RoleType.MEMBER) {
            throw new IllegalStateException("박스의 소유자 또는 멤버만 연장할 수 있습니다.");
        }

        if (box.getCount() <= 0) {
            throw new IllegalStateException("더 이상 연장할 수 없습니다.");
        }

        box.extendBoomDate();
        
        // 바로 반영 안되서 추가
        boxRepository.flush();

        // 폭파 예정 목록에 포함되어 있었다면 취소 
        handleBoomDateChange(box);
    }

    // 예약된 작업 목록 출력
    private void printScheduledTasks() {
        System.out.println("현재 예약된 삭제 작업 목록:");
        if (scheduledTasks.isEmpty()) {
            System.out.println("예약된 작업이 없습니다.");
        } else {
            for (Map.Entry<Long, ScheduledTaskInfo> entry : scheduledTasks.entrySet()) {
                System.out.println("박스 ID: " + entry.getKey() + " | 작업 상태: " +
                        (entry.getValue().getScheduledTask().isDone() ? "완료됨" : "진행 중") +
                        " | 예약된 BoomDate: " + entry.getValue().getBoomDate());
            }
        }
    }

    // 예약 작업 정보 클래스
    public static class ScheduledTaskInfo {
        private ScheduledFuture<?> scheduledTask;
        private LocalDateTime boomDate;

        // 생성자
        public ScheduledTaskInfo(ScheduledFuture<?> scheduledTask, LocalDateTime boomDate) {
            this.scheduledTask = scheduledTask;
            this.boomDate = boomDate;
        }

        public ScheduledFuture<?> getScheduledTask() {
            return scheduledTask;
        }

        public void setScheduledTask(ScheduledFuture<?> scheduledTask) {
            this.scheduledTask = scheduledTask;
        }

        public LocalDateTime getBoomDate() {
            return boomDate;
        }
 
        public void setBoomDate(LocalDateTime boomDate) {
            this.boomDate = boomDate;
        }
    }
}
