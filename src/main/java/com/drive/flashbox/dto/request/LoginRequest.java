package com.drive.flashbox.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@Builder
public class LoginRequest {
    String email;
    String password;
}
