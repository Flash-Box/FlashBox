package com.drive.flashbox.dto.response;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import com.drive.flashbox.entity.Box;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoxResponse {
	private Long bid;
	
	private String name;
    
    private LocalDate eventStartDate;

    private LocalDate eventEndDate;
    
    private LocalDate boomDate;
    
    private LocalDate modifiedDate;
    
    private List<BoxUserResponse> members; // 모임원 리스트 추가 ------------------- SCRUM-30-view-members
    
    private List<String> images;  // ✅ Picture 테이블에서 가져올 이미지 URL 리스트
    
    // entity -> dto	코드 추가 ----------------------------------------------- SCRUM-30-view-members
    public static BoxResponse from(Box box, List<BoxUserResponse> members, List<String> images) {
    	return new BoxResponse(box.getBid(),
    							box.getName(),
    							box.getEventStartDate().toLocalDate(),
    							box.getEventEndDate().toLocalDate(),
    							box.getBoomDate() != null ? box.getBoomDate().toLocalDate() : null,	//수정 -- SCRUM-30-view-members
    							box.getModifiedDate().toLocalDate(),
    							members != null ? members : Collections.emptyList(),  // ✅ null 방지 (빈 리스트 반환)
						        images != null ? images : Collections.emptyList()  // ✅ null 방지 (빈 리스트 반환)
		);
    }

	public static BoxResponse from(Box box) {
		return new BoxResponse(box.getBid(),
				box.getName(),
				box.getEventStartDate().toLocalDate(),
				box.getEventEndDate().toLocalDate(),
				box.getBoomDate() != null ? box.getBoomDate().toLocalDate() : null,	//수정 -- SCRUM-30-view-members
				box.getModifiedDate().toLocalDate(),
				null, null);
	}
}
