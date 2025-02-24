package com.drive.flashbox.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PictureUploadRequest {
    private MultipartFile[] files;
}