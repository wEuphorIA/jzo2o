package com.jzo2o.common.expcetions;

/**
 * @author Mr.M
 * @version 1.0
 * @description 标准异常基础类
 * @date 2024/4/17 11:11
 */
public abstract class AbstractException extends RuntimeException {

    public abstract int getCode();

    public abstract String getMessage();
}
