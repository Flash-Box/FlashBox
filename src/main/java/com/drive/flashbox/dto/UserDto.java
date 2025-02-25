package com.drive.flashbox.dto;

import com.drive.flashbox.entity.Box;
import com.drive.flashbox.entity.BoxUser;
import com.drive.flashbox.entity.Picture;
import com.drive.flashbox.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
@Setter
@Getter
@Builder
public class UserDto {

    private Long id;
    private String name;
    private String email;
    private String password;

    public static UserDto of(Long uid, String name, String email, String password) {
        return new UserDto(uid, name, email, password);
    }

    public static UserDto from(User user) {
        return UserDto.of(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPassword()
        );
    }

    private final List<Box> boxes = new ArrayList<>();

    private final List<Picture> pictures = new ArrayList<>();

    private final List<BoxUser> boxUsers = new ArrayList<>();
}