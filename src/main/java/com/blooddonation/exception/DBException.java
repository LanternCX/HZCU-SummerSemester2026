package com.blooddonation.exception;

/**
 * 将数据库访问异常统一转换为业务可处理的运行时异常。
 */
public class DBException extends RuntimeException {
    /**
     * 创建数据库异常。
     *
     * @param message 异常说明
     * @param cause 原始异常
     */
    public DBException(String message, Throwable cause) {
        super(message, cause);
    }
}
