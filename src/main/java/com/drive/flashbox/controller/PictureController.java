package com.drive.flashbox.controller;

import com.drive.flashbox.dto.PictureDto;
import com.drive.flashbox.dto.request.PictureUploadRequest;
import com.drive.flashbox.dto.response.PictureDownloadResponse;
import com.drive.flashbox.dto.response.PictureUploadResponse;
import com.drive.flashbox.service.PictureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/box")
@RequiredArgsConstructor
public class PictureController {

    private final PictureService pictureService;

    @GetMapping("/{bid}/picture/{pid}")
    @ResponseBody
    public ResponseEntity<PictureDto> getPictureDetails(@PathVariable Long bid, @PathVariable Long pid) {
        PictureDto pictureDTO = pictureService.getPictureDetails(bid, pid);
        return ResponseEntity.ok(pictureDTO);
    }

    // 이미지 여러 장 다운로드 링크 생성
    @GetMapping("/{bid}/picture/download")
    @ResponseBody
    public ResponseEntity<PictureDownloadResponse> getPictureDownloadUrls(
            @PathVariable Long bid,
            @RequestParam(name = "pid") List<Long> pids
    ) {
        // PictureService에서 Pre-signed URL 목록 생성
        List<String> downloadUrls = pictureService.generateDownloadUrls(bid, pids);

        PictureDownloadResponse response = PictureDownloadResponse.builder()
                .message("이미지 다운로드 링크입니다.")
                .downloadUrls(downloadUrls)
                .status(200)
                .build();

        return ResponseEntity.ok(response);
    }

    // 이미지 업로드
    @PostMapping("/{bid}/picture")
    @ResponseBody
    public ResponseEntity<PictureUploadResponse> uploadPictures(
            @PathVariable Long bid,
            @ModelAttribute PictureUploadRequest request) {
            //@RequestHeader("Authorization") String token) {
        // (여기서 토큰 검증 로직 추가 가능)
        List<Long> pictureIds = pictureService.uploadPictures(bid, request.getFiles());
        PictureUploadResponse response = PictureUploadResponse.builder()
                .message("이미지 업로드에 성공했습니다.")
                .pictureIds(pictureIds)
                .status(HttpStatus.CREATED.value())
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @DeleteMapping("/{bid}/picture/{pid}")
    @ResponseBody
    public ResponseEntity<Void> deletePicture(@PathVariable Long bid, @PathVariable Long pid) {
        pictureService.deletePicture(bid, pid);
        return ResponseEntity.noContent().build(); 
    }
    
}