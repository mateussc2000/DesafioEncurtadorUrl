package com.encurtador_url.SuperApp.exception;

import lombok.Getter;

@Getter
public class AbstractException extends RuntimeException {

    private String logcode;

        public AbstractException(String logcode, String message) {
            super(message);
            this.logcode = logcode;
        }
        public AbstractException(String logcode, String message, Throwable cause) {
            super(message, cause);
            this.logcode = logcode;
        }

    public AbstractException(String message) {
        super(message);
    }
}
