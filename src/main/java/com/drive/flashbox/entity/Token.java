package com.drive.flashbox.entity;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "token", timeToLive = 24 * 60 * 60 * 7) // 7일
@AllArgsConstructor
@Getter
@ToString
public class Token {
    @Id
    private Long uid;
    private String refreshToken;

}
