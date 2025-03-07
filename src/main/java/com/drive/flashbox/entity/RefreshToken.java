package com.drive.flashbox.entity;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "user", timeToLive = 24 * 60 * 60 * 7) // 7일
@AllArgsConstructor
@Getter
@ToString
public class RefreshToken {
    @Id
    private Long uid;
    private String refreshToken;

}
