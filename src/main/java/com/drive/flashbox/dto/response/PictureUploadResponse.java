package com.drive.flashbox.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PictureUploadResponse {
    private String message;
    private List<Long> pictureIds;
    private Long uid;
    private int status;
}