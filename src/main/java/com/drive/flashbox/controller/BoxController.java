package com.drive.flashbox.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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
import com.drive.flashbox.entity.User;
import com.drive.flashbox.repository.UserRepository;
import com.drive.flashbox.service.BoxService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class BoxController {
	private final BoxService boxService;
	private final UserRepository userRepository; // 특정 박스 모임원 조회 위해 특정 유저로 박스 생성 확인 작업 --- SCRUM-30-view-members
	
	// box 생성 페이지
	@GetMapping("/box")
	public String newBox() {
		return "newBox";
	}

    // 박스 전체 조회: HTML 페이지 반환
    @GetMapping("/boxes")
    public String getAllBoxes(Model model) {
        List<Box> boxes = boxService.getAllBoxes();
        model.addAttribute("boxes", boxes);
        return "box-list"; // templates/box-list.html 렌더링
    }

    // 박스 상세 조회: HTML 페이지 반환
    @GetMapping("/box/{bid}")
    public String getBoxById(@PathVariable("bid") Long boxId, Model model) {
        BoxResponse box = boxService.getBox(boxId);
        model.addAttribute("box", box);
        return "box-detail"; // templates/box-detail.html 렌더링
    }
	
	
	// box 다운
	@GetMapping("/box/{bid}/download")
	@ResponseBody
	public Map<String, Object> downloadBox(@PathVariable Long bid,
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
	public ResponseEntity<String> createBox(	// String -> ResponseEntity<String>, 특정 유저로 박스 생성하여 모임원 조회 작업 ----- SCRUM-30-view-members
            @RequestParam(name = "name") String name,
            @RequestParam(name = "eventStartDate") 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate eventStartDate,
            @RequestParam(name = "eventEndDate") 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate eventEndDate,
            @RequestParam(name = "uid") Long uid // 특정 박스 모임원 조회 위해 특정 유저로 박스 생성 확인 작업 ----- SCRUM-30-view-members
//            ModelMap modelMap		// 주석처리, 특정 박스 모임원 조회 위해 특정 유저로 박스 생성 확인 작업 ----- SCRUM-30-view-members
    ) {	
		// 코드 추가, 특정 박스 모임원 조회 위해 특정 유저로 박스 생성 확인 작업 ----- SCRUM-30-view-members
		User user = userRepository.findById(uid)
	            .orElseThrow(() -> new IllegalArgumentException("User를 찾을 수 없습니다: " + uid));
	    BoxRequest boxRequest = new BoxRequest(name, eventStartDate, eventEndDate);
	    boxService.createBox(boxRequest, user); // 수정된 메서드 호출
	    return ResponseEntity.ok("Box 생성 성공");
		
	    // 아래 주석처리, 특정 박스 모임원 조회 위해 특정 유저로 박스 생성 확인 작업 ----- SCRUM-30-view-members
//		BoxRequest boxRequest = new BoxRequest(name, eventStartDate, eventEndDate);
//		boxService.createBox(boxRequest);
		
		// 생성 후 box 목록 페이지로 가야하는 데 아직 없어서 임의로 지정
//		return "redirect:/box";
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
	public ResponseEntity<Void> updateBox(
	    @PathVariable("bid") Long bid,
	    @RequestBody BoxRequest boxDto  // JSON을 받을 수 있도록 변경
	) {
	    boxService.updateBox(bid, boxDto);
	    return ResponseEntity.ok().build();  // 리디렉션 대신 상태 코드 반환
	}
	
	// box에 다른 User 초대
	@PostMapping("/box/{bid}/members")
	public ResponseEntity<?> inviteUserToBox(
	    @PathVariable("bid") Long boxId,
	    @RequestParam("uid") Long userId
	) {
	    // Service 호출
	    boxService.inviteUserToBox(boxId, userId);

	    // 필요하다면 결과 DTO나 메시지를 담아서 반환
	    return ResponseEntity.ok("유저 초대가 완료되었습니다.");
  }
	
  // box 삭제 기능
	@DeleteMapping("/box/{bid}")
	public ResponseEntity<Void> deleteBox(@PathVariable("bid") Long bid) {
	    boxService.deleteBox(bid);
	    return ResponseEntity.ok().build(); // 204 No Content 대신 200 OK 반환
	}
}
