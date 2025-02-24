package com.drive.flashbox.dto;

import com.drive.flashbox.entity.User;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@ToString
@Getter
@Setter
@Builder
public class UserDTO{

    private Long uid;
    private String name;
    private String email;
    private String password;
    private LocalDateTime createdDate;


    public static UserDTO of(Long uid, String name, String email, String password, LocalDateTime createdDate) {
        return new UserDTO(uid, name, email, password, createdDate);
    }

    public static UserDTO from(User user) {
        return UserDTO.of(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPassword(),
                user.getCreatedDate()
        );
    }



//    public User toEntity() {
//        return User.of(
//                name,
//                email,
//                password
//        );
//    }




}



