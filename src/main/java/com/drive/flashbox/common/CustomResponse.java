package com.drive.flashbox.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomResponse<T> {
    private int status;
    private boolean success;
    private String message;
    private T data;
}

