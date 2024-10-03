package com.xuecheng.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义异常
     * @param e
     * @return
     */
    @ExceptionHandler(XueChengPlusException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)//用于指定 HTTP 响应的状态码
    public RestErrorResponse customException(XueChengPlusException e){
        //打印日志
        log.error("系统异常:{}",e.getErrMessage(),e);
        //将该异常信息，封装为与前端约定的对象RestErrorResponse
        return new RestErrorResponse(e.getErrMessage());
    }

    /**
     * 处理非自定义异常
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)//用于指定 HTTP 响应的状态码
    public RestErrorResponse exception(Exception e){
        //打印日志
        log.error("系统异常{}",e.getMessage(),e);
        //将该异常信息，封装为与前端约定的对象RestErrorResponse
        return new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());
    }

}
