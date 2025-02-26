package com.drive.flashbox.dto.request;

import com.drive.flashbox.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@Builder
@NoArgsConstructor // JSON을 객체로 변환할 때 기본 생성자를 찾지 못하는 문제 해결 위해 추가 ------ SCRUM-30-view-members
@AllArgsConstructor // @Builder가 @NoArgsConstructor와 함께 작동 위해 AllArgsConstructor 추가 ------ SCRUM-30-view-members
public class SignupRequestDTO {
    private String name;
    private String email;
    private String password;

    public User toEntity() {
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

