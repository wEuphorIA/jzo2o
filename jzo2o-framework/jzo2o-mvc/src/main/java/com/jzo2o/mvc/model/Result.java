package com.jzo2o.mvc.model;

import cn.hutool.http.HttpStatus;
import com.jzo2o.common.constants.HeaderConstants;
import com.jzo2o.mvc.utils.RequestUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class Result <T> {

    public static final String REQUEST_OK = "OK";

    @ApiModelProperty(value = "成功标志，true-成功，false-失败")
    private boolean success=true;
    @ApiModelProperty(value = "业务状态码，200-成功，其它-失败")
    private int code;
    @ApiModelProperty(value = "响应消息", example = "OK")
    private String message;
    private String msg;

    @ApiModelProperty(value = "响应数据")
    private T result;
    private T data;
    @ApiModelProperty(value = "请求id", example = "1af123c11412e")
    private String requestId;

    public static Result<Void> ok() {
        return new Result<Void>(HttpStatus.HTTP_OK, REQUEST_OK, null);
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(HttpStatus.HTTP_OK, REQUEST_OK, data);
    }

    public static <T> Result<T> error(String msg) {
        Result<T> objectResult = new Result<>(HttpStatus.HTTP_BAD_REQUEST, msg, null);
        objectResult.setSuccess(false);
        return objectResult;
    }

    public static <T> Result<T> error(int code, String msg) {
        Result<T> objectResult = new Result<>(code, msg, null);
        objectResult.setSuccess(false);
        return objectResult;
    }

    public Result() {
        this.requestId = RequestUtils.getValueFromHeader(HeaderConstants.REQUEST_ID);
    }

    public Result(int code, String msg, T data) {
        this.code = code;
        this.message = msg;
        this.msg = msg;
        this.result = data;
        this.data = data;
        this.success=true;
        this.requestId = RequestUtils.getValueFromHeader(HeaderConstants.REQUEST_ID);
    }

    public boolean success() {
//        return code == HttpStatus.HTTP_OK;
        return success;
    }
}
