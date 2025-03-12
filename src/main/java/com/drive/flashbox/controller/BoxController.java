package com.drive.flashbox.controller;

import java.util.*;

import com.drive.flashbox.common.CustomResponse;
import com.drive.flashbox.dto.response.BoxCreateResponse;
import com.drive.flashbox.dto.response.LoginResponse;
import com.drive.flashbox.entity.User;
import com.drive.flashbox.security.FBUserDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.drive.flashbox.dto.request.BoxRequest;
import com.drive.flashbox.dto.response.BoxResponse;
import com.drive.flashbox.entity.Box;
import com.drive.flashbox.entity.Picture;
import com.drive.flashbox.repository.BoxRepository;
import com.drive.flashbox.repository.UserRepository;
import com.drive.flashbox.security.FBUserDetails;
import com.drive.flashbox.service.BoxService;
import com.drive.flashbox.service.PictureService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class BoxController {
	private final BoxService boxService;
	private final UserRepository userRepository; // 특정 박스 모임원 조회 위해 특정 유저로 박스 생성 확인 작업 --- SCRUM-30-view-members
    private final PictureService pictureService; // ✅ PictureService 주입
    private final BoxRepository boxRepository; // BoxRepository 주입
	
	// box 생성 페이지
	@GetMapping("/box")
	public String newBox() {
		return "newBox";
	}

    // 박스 전체 조회: HTML 페이지 반환
    @GetMapping("/boxes")
    public String getAllBoxes() {

        return "main"; // templates/main.html 렌더링
    }

	@GetMapping("/api/boxes")
	public ResponseEntity<CustomResponse<?>> getAllUserBoxes(@AuthenticationPrincipal FBUserDetails fbUserDetails) {
		List<BoxResponse> boxes = boxService.getAllUserBoxes(fbUserDetails.getUid());

		if (boxes == null) {
			CustomResponse<Object> response = new CustomResponse<Object>(
					HttpStatus.BAD_REQUEST.value(),
					false,
					"유저 박스 전체조회 실패",
					null
					);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		CustomResponse<Object> response = new CustomResponse<>(
				HttpStatus.OK.value(),
				true,
				"유저 박스 전체조회 성공",
				boxes
		);
//		System.out.println(boxes);
		return ResponseEntity.ok(response);
	}
    
    
    // 박스 상세 조회: HTML 페이지 반환
    @GetMapping("/box/{bid}")
    public String getBoxById(@PathVariable("bid") Long boxId, Model model) {
        BoxResponse box = boxService.getBox(boxId); // ✅ 박스 정보 가져오기
        List<Picture> images = pictureService.findByBoxId(boxId); // ✅ 객체를 통해 호출

        model.addAttribute("box", box);
        model.addAttribute("images", images); // ✅ 이미지 리스트도 추가

        return "box-detail"; // templates/box-detail.html 렌더링
    }

		
	// box 다운
	@GetMapping("/box/{bid}/download")
	@ResponseBody
	public Map<String, Object> downloadBox(@PathVariable("bid") Long bid,
										   @RequestParam(value = "uid", required = false) Long uid) {
		if (uid == null) {
			uid = 1L; // 기본 테스트 사용자
		}
		String downloadUrl = boxService.generateZipAndGetPresignedUrl(bid, uid);

		Map<String, Object> response = new HashMap<>();
		response.put("message", "박스 다운로드 링크 생성에 성공하였습니다");
		response.put("downloadUrl", downloadUrl);
		response.put("status", 200);
		return response;
	}
	
	// box 생성 기능
	@PostMapping("/box")
  @ResponseBody // JSON 응답으로 변경, 특정 박스 모임원 조회 위해 특정 유저로 박스 생성 확인 작업 --------- SCRUM-30-view-members
	public ResponseEntity<CustomResponse<BoxCreateResponse>> createBox( // String -> ResponseEntity<String>, 특정 유저로 박스 생성하여 모임원 조회 작업 ----- SCRUM-30-view-members
			@RequestBody BoxRequest boxRequest,
			@AuthenticationPrincipal FBUserDetails fbUserDetails
    ) {
		Long uid = fbUserDetails.getUid();

		// 코드 추가, 특정 박스 모임원 조회 위해 특정 유저로 박스 생성 확인 작업 ----- SCRUM-30-view-members
		User user = userRepository.findById(uid)
				.orElseThrow(() -> new IllegalArgumentException("User를 찾을 수 없습니다: " + uid));

		BoxCreateResponse data = boxService.createBox(boxRequest, uid);

		CustomResponse<BoxCreateResponse> response = new CustomResponse<>(
				HttpStatus.OK.value(),
				true,
				"Box 생성 성공",
				data
		);

		return ResponseEntity.ok(response);
	}
	
	// box 수정 페이지
	@GetMapping("/box/{bid}/edit")
	public String editbox(@PathVariable("bid") Long bid, ModelMap modelMap) {
		
		// 박스 정보 담아서 수정 페이지 이동
		modelMap.addAttribute("box",boxService.getBox(bid));
		
		return "editBox";
	}
	
	// box 수정 기능
	@PutMapping("/box/{bid}")
	public ResponseEntity<CustomResponse<BoxResponse>> updateBox(
	    @PathVariable("bid") Long bid,
	    @RequestBody BoxRequest boxDto,  // JSON을 받을 수 있도록 변경
	    @AuthenticationPrincipal FBUserDetails fbUserDetails
	) {
		Long uid = fbUserDetails.getUid();
		try {
			BoxResponse data = boxService.updateBox(bid, uid, boxDto);
			CustomResponse<BoxResponse> response = new CustomResponse<>(
					HttpStatus.OK.value(),
					true,
					"Box 수정 성공",
					data
			);

			return ResponseEntity.ok(response);
		}
		catch (IllegalStateException e) {
	        throw e;
	    }
	}
	
	// box에 다른 User 초대 페이지 표시
	@GetMapping("/box/{bid}/members")
	public String showInvitePage(@PathVariable("bid") Long boxId, Model model) {
	    
		// boxId에 해당하는 Box 정보를 가져와서 갤러리 이름 설정
	    Box box = boxRepository.findById(boxId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 박스 ID"));
		List<User> users = userRepository.findUsersByBoxId(boxId);
	    
	    model.addAttribute("boxId", boxId); // 박스 ID 전달
	    model.addAttribute("users", users); // 기존 참여 유저 리스트 전달

	    // 갤러리 이름 설정 (예제: "내 갤러리" → 실제 데이터와 연결 가능)
	    model.addAttribute("galleryName", box.getName()); 

	    return "user_invite"; // Thymeleaf 템플릿 반환
	}
	
	// box에 다른 User 초대하는 동작 API
	@PostMapping("/box/{bid}/members")
	public ResponseEntity<Map<String, Object>> inviteUserToBox(
	    @PathVariable("bid") Long boxId,
	    @RequestParam("email") String email // ✅ email을 받아 userId 찾기
	) {
	    Map<String, Object> response = new HashMap<>();

	    // email로 user 조회
	    Optional<User> userOptional = userRepository.findByEmail(email);
	    if (userOptional.isEmpty()) {
	        response.put("success", false);
	        response.put("message", "해당 이메일의 유저가 존재하지 않습니다.");
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	    }

	    User user = userOptional.get();
	    boxService.inviteUserToBox(boxId, user.getId());

	    response.put("success", true);
	    response.put("message", "초대가 완료되었습니다.");
	    return ResponseEntity.ok(response);
	}

	
	// box 삭제 기능
	@DeleteMapping("/box")
	public ResponseEntity<CustomResponse<Object>> deleteBoxes(
	        @RequestBody List<Long> bidList,	// 여러 박스 한번에 삭제할 때 json 형태로 받아와야함 ex) [1, 2, 4]
	        @AuthenticationPrincipal FBUserDetails fbUserDetails
	) {
	    Long uid = fbUserDetails.getUid();
	    try {
	        boxService.deleteBoxes(bidList, uid);
			CustomResponse<Object> response = new CustomResponse<>(
					HttpStatus.OK.value(),
					true,
					"Box 삭제 성공",
					null
			);

			return ResponseEntity.ok(response);
	    } catch (IllegalStateException e) {
	        throw e;
	    }
	}
	
	// box 폭파 기한 연장
	@PostMapping("/box/{bid}/extend")
	public ResponseEntity<CustomResponse<Object>> extendBoomDate(
			@PathVariable("bid") Long bid,
			@AuthenticationPrincipal FBUserDetails fbUserDetails
	){
		Long uid = fbUserDetails.getUid();
		try {
	        boxService.extendBoomDate(bid, uid);
	        CustomResponse<Object> response = new CustomResponse<>(
					HttpStatus.OK.value(),
					true,
					"Box 연장 성공",
					null
			);
	        return ResponseEntity.ok(response);
	    } catch (IllegalStateException e) {	
	    	CustomResponse<Object> response = new CustomResponse<>(
					HttpStatus.BAD_REQUEST.value(),
					false,
					e.getMessage(),
					null
			);
	    	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	    }
	}
}
