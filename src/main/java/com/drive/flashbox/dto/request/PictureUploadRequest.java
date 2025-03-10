package com.drive.flashbox.dto.request;

import lombok.Data;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Data
@Setter  // <-- Lombok의 Setter 추가
public class PictureUploadRequest {
    private MultipartFile[] files;
}