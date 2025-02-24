package com.drive.flashbox.dto;

import com.drive.flashbox.entity.Box;
import com.drive.flashbox.entity.BoxUser;
import com.drive.flashbox.entity.Picture;
import com.drive.flashbox.entity.User;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class UserDto {

    private Long id;
    private String name;
    private String email;
    private String password;
    private final List<Box> boxes = new ArrayList<>();
    private final List<Picture> pictures = new ArrayList<>();
    private final List<BoxUser> boxUsers = new ArrayList<>();

    public static UserDto from(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .password(user.getPassword())
                // 필요한 경우 boxes, pictures, boxUsers 필드도 변환
                .build();
    }
}
