package com.drive.flashbox.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PictureDownloadResponse {
    private String message;
    private String downloadUrl;
    private int status;
}