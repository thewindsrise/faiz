package org.thewindsrise.faiz.base;

/**
 * 内部使用的异常。
 *
 * @author: wjf
 * @date: 2024/6/2
 */
public class FaizException extends RuntimeException{

    public FaizException(Exception exception) {
        super(exception);
    }

    public FaizException(String message) {
        super(message);
    }

    public static void throwFaizException(Exception exception) throws FaizException {
        throw new FaizException(exception);
    }

    public static void throwFaizException(String message) throws FaizException {
        throw new FaizException(message);
    }

}
