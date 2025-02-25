package com.drive.flashbox.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import com.drive.flashbox.security.FBUserDetails;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.drive.flashbox.dto.request.BoxRequest;
import com.drive.flashbox.service.BoxService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class BoxController {
	private final BoxService boxService;
	
	// box 생성 페이지
	@GetMapping("/box")
	public String newBox() {
		return "newBox";
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
	public String createBox(
//            @RequestParam(name = "name") String name,
//            @RequestParam(name = "eventStartDate")
//            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate eventStartDate,
//            @RequestParam(name = "eventEndDate")
//            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate eventEndDate,
			@RequestBody BoxRequest boxRequest,
			@AuthenticationPrincipal FBUserDetails fbUserDetails
//            ModelMap modelMap
    ) {
		
//		BoxRequest boxRequest = new BoxRequest(name, eventStartDate, eventEndDate);
		boxService.createBox(boxRequest, fbUserDetails.getUid());
		
		// 생성 후 box 목록 페이지로 가야하는 데 아직 없어서 임의로 지정
		return "redirect:/box";
	}
	
	// box 수정 페이지
	@GetMapping("/box/{bid}")
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
}
