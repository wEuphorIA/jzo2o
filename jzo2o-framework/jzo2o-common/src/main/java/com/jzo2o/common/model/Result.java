package com.jzo2o.common.model;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.http.HttpStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jzo2o.common.constants.HeaderConstants;
import com.jzo2o.common.utils.IoUtils;
import com.jzo2o.common.utils.NumberUtils;
import com.jzo2o.common.utils.StringUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;

@Data
public class Result<T> {

    public static final int SUCCESS = 200;
    public static final int ERROR = 500;
    public static final String OK = "OK";
    public static final int FAILED = 1;

    public static final byte[] OK_BYTES =
            String.format("{\"code\":%d,\"message\":\"%s\",\"msg\":\"%s\",\"success\":true,\"result\": null,\"data\": null}", SUCCESS, OK, OK).getBytes(StandardCharsets.UTF_8);
    public static final byte[] OK_PREFIX =
            String.format("{\"code\":%d,\"message\":\"%s\",\"msg\":\"%s\",\"success\":true,\"result\": ", SUCCESS, OK, OK).getBytes(StandardCharsets.UTF_8);

    public static final byte[] OK_SUFFIX_DATA = ",\"data\": ".getBytes(StandardCharsets.UTF_8);
    public static final byte[] OK_SUFFIX = "}".getBytes(StandardCharsets.UTF_8);
    public static final byte[] OK_STR_PREFIX =
            String.format("{\"code\":%d,\"message\":\"%s\",\"msg\":\"%s\",\"success\":true,\"result\":\"", SUCCESS, OK, OK).getBytes(StandardCharsets.UTF_8);
    public static final byte[] OK_STR_SUFFIX_DATA = "\",\"data\": \"".getBytes(StandardCharsets.UTF_8);
    public static final byte[] OK_STR_SUFFIX = "\"}".getBytes(StandardCharsets.UTF_8);
    public static final String REQUEST_OK = "OK";
    /**
     * 成功标志
     */
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

    public static byte[] error(int code, String msg) throws JsonProcessingException {
        Result<Object> objectResult = new Result<>(code, msg, null);
        objectResult.setSuccess(false);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(objectResult);
        return json.getBytes(StandardCharsets.UTF_8);
    }

    public Result() {
    }

    public Result(int code, String msg, T data) {
        this.code = code;
        this.message = msg;
        this.msg = msg;
        this.result = data;
        this.data = data;
        this.success=true;
    }

    public boolean success() {
//        return code == HttpStatus.HTTP_OK;
        return success;
    }

    public static byte[] plainOk() {
        return OK_BYTES;
    }


//    public static byte[] plainOk(byte[] data) {
//        if(data == null || data.length <= 0){
//            return OK_BYTES;
//        }
//        ObjectMapper objectMapper = new ObjectMapper();
//        String jsonString = "{\"code\":200,\"msg\":\"OK\",\"data\": {}}";
//
//        ObjectNode jsonNode = null;
//        try {
//            jsonNode = objectMapper.readValue(jsonString, ObjectNode.class);
//            String dataString = new String(data, StandardCharsets.UTF_8);
//            // 2. 设置属性值
//            jsonNode.put("data", dataString);
//            // 3. 将 ObjectNode 对象转换回 JSON 字符串
//            String updatedJsonString = objectMapper.writeValueAsString(jsonNode);
//            return updatedJsonString.getBytes(StandardCharsets.UTF_8);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//    }
    public static byte[] plainOk(byte[] data) {
        if(data == null || data.length <= 0){
            return OK_BYTES;
        }
        byte b = data[0];
        if (b == 91 || b == 123 || b==34) {//[  {  "
            return ArrayUtil.addAll(OK_PREFIX, data,OK_SUFFIX_DATA,data, OK_SUFFIX);
        }
//        if(StringUtils.isStr(data)){
//            return ArrayUtil.addAll(OK_PREFIX, data, OK_SUFFIX);
//        }
        //将data转为字符串
        String s = new String(data, StandardCharsets.UTF_8);
        if("false".equals(s) || "true".equals(s) || NumberUtils.isNumber(s)){
            return ArrayUtil.addAll(OK_PREFIX, data,OK_SUFFIX_DATA,data, OK_SUFFIX);
        }
        return ArrayUtil.addAll(OK_STR_PREFIX, data,OK_STR_SUFFIX_DATA,data, OK_STR_SUFFIX);
    }
    public static byte[] plainError(byte[] data) {
        if(data == null || data.length <= 0){
            return OK_BYTES;
        }
        byte b = data[0];
        if (b == 91 || b == 123) {
            return ArrayUtil.addAll(OK_PREFIX, data,OK_SUFFIX_DATA,data, OK_SUFFIX);
        }
        //将data转为字符串
        String s = new String(data, StandardCharsets.UTF_8);
        if("false".equals(s) || "true".equals(s) || NumberUtils.isNumber(s)){
            return ArrayUtil.addAll(OK_PREFIX, data,OK_SUFFIX_DATA,data, OK_SUFFIX);
        }
        return ArrayUtil.addAll(OK_STR_PREFIX, data,OK_STR_SUFFIX_DATA,data, OK_STR_SUFFIX);
    }
}
