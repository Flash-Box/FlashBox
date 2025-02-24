package com.drive.flashbox.dto.request;

import java.time.LocalDate;

import com.drive.flashbox.entity.Box;
import com.drive.flashbox.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoxRequest {
    private String name;
    
    private LocalDate eventStartDate;

    private LocalDate eventEndDate;
    

    // dto -> entity
    public static Box toEntity(BoxRequest boxDto, User user) {
    	return new Box(boxDto.getName(),
    			boxDto.getEventStartDate().atStartOfDay(),
    			boxDto.getEventEndDate().atStartOfDay().plusDays(1).minusSeconds(1),
    			user);
    	
    }
    
    // entity -> dto
    public static BoxRequest from(Box box) {
    	return new BoxRequest(box.getName(), box.getEventStartDate().toLocalDate(), box.getEventEndDate().toLocalDate());
    }
}
