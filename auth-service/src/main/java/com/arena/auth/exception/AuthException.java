package com.arena.auth.exception;

// O facem generică pentru tot ce ține de login/register/auth
public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}