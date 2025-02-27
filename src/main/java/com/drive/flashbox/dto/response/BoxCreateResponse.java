package com.drive.flashbox.dto.response;

import com.drive.flashbox.dto.UserDto;
import com.drive.flashbox.entity.Box;
import com.drive.flashbox.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Builder
@ToString
public class BoxCreateResponse {

    private Long bid;

    private String name;

    private LocalDate eventStartDate;

    private LocalDate eventEndDate;

    private LocalDate boomDate;

    private LocalDate modifiedDate;

    private UserResponse user;

    @Getter
    @AllArgsConstructor
    static class UserResponse{
        Long id;
        String name;
    }

    public static BoxCreateResponse of(Box box, User user){
        return BoxCreateResponse.builder()
                .bid(box.getBid())
                .name(box.getName())
                .eventStartDate(box.getEventStartDate().toLocalDate())
                .eventEndDate(box.getEventEndDate().toLocalDate())
                .boomDate(box.getBoomDate().toLocalDate())
                .modifiedDate(box.getModifiedDate().toLocalDate())
                .user(new UserResponse(user.getId(),user.getName()))
                .build();
    }


}
