package com.drive.flashbox.dto.response;

import java.time.LocalDate;

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
    
 // entity -> dto
    public static BoxResponse from(Box box) {
    	return new BoxResponse(box.getBid(),
    							box.getName(),
    							box.getEventStartDate().toLocalDate(),
    							box.getEventEndDate().toLocalDate(),
    							box.getBoomDate().toLocalDate(),
    							box.getModifiedDate().toLocalDate());
    }
}
