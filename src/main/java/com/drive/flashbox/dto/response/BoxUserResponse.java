package com.drive.flashbox.dto.response;

import java.time.LocalDateTime;

import com.drive.flashbox.entity.enums.RoleType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BoxUserResponse {	// 클래스 생성 ---------------------- SCRUM-30-view-members

	private Long id;
    private Long userId;
    private String userName; // 유저 이름을 추가해서 클라이언트가 보기 편리하게
    private LocalDateTime participateDate;
    private RoleType role;

    public static BoxUserResponse from(com.drive.flashbox.entity.BoxUser boxUser) {
        return BoxUserResponse.builder()
                .id(boxUser.getId())
                .userId(boxUser.getUser().getId())
                .userName(boxUser.getUser().getName())
                .participateDate(boxUser.getParticipateDate())
                .role(boxUser.getRole())
                .build();
    }
    
}
