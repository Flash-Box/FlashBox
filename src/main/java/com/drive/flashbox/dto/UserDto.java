package com.drive.flashbox.dto;

<<<<<<< HEAD
import com.drive.flashbox.entity.User;
import lombok.*;
=======
import com.drive.flashbox.entity.Box;
import com.drive.flashbox.entity.BoxUser;
import com.drive.flashbox.entity.Picture;
import com.drive.flashbox.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
>>>>>>> 337f15372d7686393d4a120931bdc66cc4e35ca3

import java.time.LocalDateTime;

<<<<<<< HEAD
@AllArgsConstructor
@ToString
=======
@ToString
@Setter
>>>>>>> 337f15372d7686393d4a120931bdc66cc4e35ca3
@Getter
@Setter
@Builder
<<<<<<< HEAD
public class UserDTO{

    private Long uid;
=======
public class UserDto {

    private Long id;
>>>>>>> 337f15372d7686393d4a120931bdc66cc4e35ca3
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


<<<<<<< HEAD
=======
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
>>>>>>> 337f15372d7686393d4a120931bdc66cc4e35ca3


<<<<<<< HEAD
}



=======
    private final List<BoxUser> boxUsers = new ArrayList<>();
}
>>>>>>> 337f15372d7686393d4a120931bdc66cc4e35ca3
