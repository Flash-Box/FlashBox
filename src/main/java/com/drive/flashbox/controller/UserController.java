package com.drive.flashbox.controller;

import com.drive.flashbox.common.CustomResponse;
import com.drive.flashbox.dto.request.SignupRequestDTO;
import com.drive.flashbox.dto.response.SignupResponseDTO;
import com.drive.flashbox.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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


}
