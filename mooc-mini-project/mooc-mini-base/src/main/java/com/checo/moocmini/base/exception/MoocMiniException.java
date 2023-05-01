package com.checo.moocmini.base.exception;

public class MoocMiniException extends RuntimeException {

    private String errMessage;

    public MoocMiniException() {
        super();
    }

    public MoocMiniException(String errMessage) {
        super(errMessage);
        this.errMessage = errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public static void castException(String errMessage) {
        throw new MoocMiniException(errMessage);
    }

    public static void castException(CommonError error) {
        throw new MoocMiniException(error.getErrMessage());
    }
}
