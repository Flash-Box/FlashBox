package com.drive.flashbox.controller;

import com.drive.flashbox.common.CustomResponse;
import com.drive.flashbox.dto.request.SignupRequestDTO;
import com.drive.flashbox.dto.response.SignupResponseDTO;
import com.drive.flashbox.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RequiredArgsConstructor
@Controller
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<CustomResponse<SignupResponseDTO>> signup(@RequestBody SignupRequestDTO signupRequestDTO) {
        SignupResponseDTO user = userService.registerUser(signupRequestDTO);
        CustomResponse<SignupResponseDTO> response = new CustomResponse<>(
                HttpStatus.CREATED.value(),
                true,
                "회원가입 성공",
                user
        );
        return ResponseEntity.ok(response);
    }

    // 회원 탈퇴
    @DeleteMapping("/unregister/{uid}")
    public ResponseEntity<String> deleteUser(@PathVariable("uid") Long uid) {
        userService.deleteUser(uid);
        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
    }


}
