package com.drive.flashbox.controller;

import com.drive.flashbox.dto.PictureDto;
import com.drive.flashbox.dto.request.PictureUploadRequest;
import com.drive.flashbox.dto.response.PictureDownloadResponse;
import com.drive.flashbox.dto.response.PictureUploadResponse;
import com.drive.flashbox.security.FBUserDetails;
import com.drive.flashbox.service.PictureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/box")
@RequiredArgsConstructor
public class PictureController {

    private final PictureService pictureService;

    // 업로드 후 이미지 랜더링 관련 GetMapping 소스코드
    @GetMapping("/{bid}/pictures")
    public ResponseEntity<List<PictureDto>> getPictures(@PathVariable("bid") Long bid) {
        List<PictureDto> pictures = pictureService.getPicturesByBoxId(bid);
        return ResponseEntity.ok(pictures);
    }
    
    
    @GetMapping("/{bid}/picture/{pid}")
    @ResponseBody
    public ResponseEntity<PictureDto> getPictureDetails(@PathVariable("bid") Long bid, @PathVariable("pid") Long pid) {
        PictureDto pictureDTO = pictureService.getPictureDetails(bid, pid);
        return ResponseEntity.ok(pictureDTO);
    }

    // 이미지 업로드
    @PostMapping("/{bid}/picture")
    @ResponseBody
    public ResponseEntity<PictureUploadResponse> uploadPictures(
            @PathVariable("bid") Long bid,
            @ModelAttribute PictureUploadRequest request,
            @AuthenticationPrincipal FBUserDetails fbUserDetails ) {
        Long uid = fbUserDetails.getUid();  // 인증된 사용자의 uid를 바로 얻음
        List<Long> pictureIds = pictureService.uploadPictures(bid, uid,request.getFiles());
        PictureUploadResponse response = PictureUploadResponse.builder()
                .message("이미지 업로드에 성공했습니다.")
                .pictureIds(pictureIds)
                .status(HttpStatus.CREATED.value())
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 이미지 다운로드: pid가 1개면 단일 파일, 여러 개면 ZIP 파일로 다운로드
    @GetMapping("/{bid}/picture/download")
    @ResponseBody
    public ResponseEntity<PictureDownloadResponse> downloadPictures(
            @PathVariable("bid") Long bid,
            @RequestParam("pid") List<Long> pids) {

        String downloadUrl = pictureService.generateDownloadUrlForPictures(bid, pids);
        PictureDownloadResponse response = PictureDownloadResponse.builder()
                .message("이미지 다운로드 링크입니다.")
                .downloadUrl(downloadUrl)
                .status(200)
                .build();
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{bid}/picture/{pid}")
    @ResponseBody
    public ResponseEntity<Void> deletePicture(@PathVariable("bid") Long bid, @PathVariable("pid") Long pid) {
        pictureService.deletePicture(bid, pid);
        return ResponseEntity.noContent().build(); 
    }
    
}