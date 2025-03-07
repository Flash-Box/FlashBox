package com.drive.flashbox.controller;

import com.drive.flashbox.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RequiredArgsConstructor
@RestController
public class TokenController {

    private final TokenService tokenService;

    @GetMapping("/get/{uid}")
    @ResponseBody
    public String getToken1(@PathVariable("uid") Long uid) {

        String token = tokenService.getToken(uid);
        return "토큰 조회 성공: " + token;
    }
    @GetMapping("/get/repo/{uid}")
    @ResponseBody
    public String getToken2(@PathVariable("uid") Long uid) {

        String token = tokenService.getTokenFromRepo(uid);
        return "토큰 조회 성공: " + token;
    }
}
