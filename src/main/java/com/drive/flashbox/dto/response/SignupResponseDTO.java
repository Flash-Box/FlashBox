package com.drive.flashbox.dto.response;

import com.drive.flashbox.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;


@Builder
@Getter
@ToString
public class SignupResponseDTO {
    Long id;
    String name;

    public static SignupResponseDTO of(User user){
        return SignupResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }


}
