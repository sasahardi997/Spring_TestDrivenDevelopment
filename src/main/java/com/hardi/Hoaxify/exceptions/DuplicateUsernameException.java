package com.hardi.Hoaxify.exceptions;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Date;

@ResponseStatus(HttpStatus.BAD_REQUEST)
@Getter
@Setter
public class DuplicateUsernameException extends RuntimeException{

    private long timeStamp = new Date().getTime();

    int status = 400;

    String message;

    public DuplicateUsernameException(String message) {
        this.message = message;
    }
}
