package com.drive.flashbox.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PictureDto {
    private Long pid;
    private String name;
    private LocalDateTime uploadDate;
    private String imageUrl;
    private Long userId; // User 엔티티 대신 ID만
    private Long boxId;  // Box 엔티티 대신 ID만
}