package com.drive.flashbox.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class LoginResponse {
    Long uid;
    String name;
    String accessToken;
    String refreshToken;
}
