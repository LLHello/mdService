package com.mdservice.utils;

import com.mdservice.constant.ResultConstant;
import lombok.Data;

@Data
public class Result<T> {
    private Short code;
    private String msg;
    private T data;
    public Result(Short code, String msg, T data){
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
    public static<T> Result<T> success(){
        return new Result<>(ResultConstant.SUCCESS_CODE,ResultConstant.SUCCESS_DEFAULT_MSG, null);
    }
    public static<T> Result<T> success(T data){
        return new Result<>(ResultConstant.SUCCESS_CODE,ResultConstant.SUCCESS_DEFAULT_MSG, data);
    }
    public static<T> Result<T> error(){
        return new Result<>(ResultConstant.ERROR_DEFAULT_CODE, ResultConstant.ERROR_DEFAULT_MSG,null);
    }
    public static<T> Result<T> error(String msg){
        return new Result<>(ResultConstant.ERROR_DEFAULT_CODE, msg, null);
    }
    public static<T> Result<T> error(Short code, String msg){
        return new Result<>(code, msg, null);
    }
}
