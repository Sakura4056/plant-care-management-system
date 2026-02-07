package com.plant.backend.exception;

import com.plant.backend.util.Result;
import com.plant.backend.util.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.error("Business Exception: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        log.error("Validation Exception: {}", e.getMessage());
        String msg = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return Result.error(ResultCode.PARAM_ERROR.getCode(), msg);
    }
    
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        log.error("Bind Exception: {}", e.getMessage());
        String msg = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return Result.error(ResultCode.PARAM_ERROR.getCode(), msg);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public Result<Void> handleBadCredentialsException(BadCredentialsException e) {
         return Result.error(ResultCode.PARAM_ERROR.getCode(), "用户名或密码错误");
    }

    @ExceptionHandler({AuthenticationException.class})
    public Result<Void> handleAuthenticationException(AuthenticationException e) {
        return Result.error(ResultCode.UNAUTHORIZED.getCode(), "认证失败: " + e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        return Result.error(ResultCode.FORBIDDEN);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class})
    public Result<Void> handleBadRequestException(Exception e) {
        log.error("Bad Request: {}", e.getMessage());
        return Result.error(ResultCode.PARAM_ERROR);
    }
    
    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<Void> handleNoHandlerFoundException(NoHandlerFoundException e) {
        return Result.error(ResultCode.NOT_FOUND);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
         return Result.error(ResultCode.PARAM_ERROR.getCode(), "请求方法不支持");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("System Error", e);
        return Result.error(ResultCode.SYSTEM_ERROR);
    }
}
