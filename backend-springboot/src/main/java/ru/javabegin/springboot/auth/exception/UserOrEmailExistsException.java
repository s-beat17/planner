package ru.javabegin.springboot.auth.exception;

import org.springframework.security.core.AuthenticationException;


// пользователь или email уже существует
// AuthenticationException - нужен для глобальной обработки всех ошибок аутентификации
public class UserOrEmailExistsException extends AuthenticationException {

    public UserOrEmailExistsException(String msg) {
        super(msg);
    }


    public UserOrEmailExistsException(String msg, Throwable t) {
        super(msg, t);
    }
}
