package com.drive.flashbox.dto.request;

import com.drive.flashbox.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@Builder
public class SignupRequestDTO {
    private String name;
    private String email;
    private String password;

    public User toEntity(){
        return User.builder()
                .name(name)
                .email(email)
                .password(password)
                .build();

    }

    public User toEntity(String encodedPassword){
        System.out.println(encodedPassword);

        return User.builder()
                .name(name)
                .email(email)
                .password(encodedPassword)
                .build();

    }

}
