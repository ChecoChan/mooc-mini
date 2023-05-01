package com.checo.moocmini.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 全局异常处理器
 */
@Slf4j
@ControllerAdvice
@Order(0)
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(MoocMiniException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse customException(MoocMiniException e) {
        log.error("系统异常：{}", e.getErrMessage(), e);
        return new RestErrorResponse(e.getErrMessage());
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse exception(Exception e) {
        log.error("系统异常：{}", e.getMessage(), e);
        if (e.getMessage().equals("不允许访问"))
            return new RestErrorResponse("您没用权限操作此功能");
        return new RestErrorResponse(CommonError.UNKNOWN_ERROR.getErrMessage());
    }
}

