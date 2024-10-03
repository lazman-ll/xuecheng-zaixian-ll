package com.xuecheng.base.exception;

/**
 * 本项目自定义异常类型
 */
public class XueChengPlusException extends RuntimeException{
    private String errMessage;

    public XueChengPlusException(String errMessage) {
        this.errMessage = errMessage;
    }

    public XueChengPlusException(String message, String errMessage) {
        super(message);
        this.errMessage = errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public static void cast(String errMessage){
        throw new XueChengPlusException(errMessage);
    }

    public static void cast(CommonError error){
        throw new XueChengPlusException(error.getErrMessage());
    }
}
