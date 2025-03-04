package com.drive.flashbox.controller;

import com.drive.flashbox.common.CustomResponse;
import com.drive.flashbox.dto.UserDto;
import com.drive.flashbox.dto.request.LoginRequest;
import com.drive.flashbox.dto.request.SignupRequestDTO;
import com.drive.flashbox.dto.response.SignupResponseDTO;
import com.drive.flashbox.entity.User;
import com.drive.flashbox.security.FBUserDetails;
import com.drive.flashbox.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@Controller
public class UserController {

    private final UserService userService;

    // 회원 탈퇴
    @DeleteMapping("/unregister/{uid}")
    public ResponseEntity<String> deleteUser(@PathVariable("uid") Long uid) {
        userService.deleteUser(uid);
        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
    }

    @GetMapping("/api/user-info")
    public ResponseEntity<?> getUserInfo(@AuthenticationPrincipal FBUserDetails fbUserDetails) {
        if (fbUserDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        User user = userService.getUser(fbUserDetails.getUid());

        return ResponseEntity.ok(Map.of("name", user.getName()));
    }


}
