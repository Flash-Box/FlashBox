package com.drive.flashbox.dto.response;

import java.time.LocalDate;
import java.util.List;

import com.drive.flashbox.entity.Box;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BoxResponse {
	private Long bid;
	
	private String name;
    
    private LocalDate eventStartDate;

    private LocalDate eventEndDate;
    
    private LocalDate boomDate;
    
    private LocalDate modifiedDate;
    
    private List<BoxUserResponse> members; // 모임원 리스트 추가 ------------------- SCRUM-30-view-members
    
    // entity -> dto	코드 추가 ----------------------------------------------- SCRUM-30-view-members
    public static BoxResponse from(Box box, List<BoxUserResponse> members) {
    	return new BoxResponse(box.getBid(),
    							box.getName(),
    							box.getEventStartDate().toLocalDate(),
    							box.getEventEndDate().toLocalDate(),
    							box.getBoomDate() != null ? box.getBoomDate().toLocalDate() : null,	//수정 -- SCRUM-30-view-members
    							box.getModifiedDate().toLocalDate(),
    							members);
    }
}
