package com.drive.flashbox.dto.response;

import com.drive.flashbox.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;


@Builder
@Getter
@ToString
public class SignupResponse {
    Long id;
    String name;

    public static SignupResponse of(User user){
        return SignupResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }


}
