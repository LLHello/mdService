package com.mdservice.handler;

import com.mdservice.constant.ResultConstant;
import com.mdservice.exception.AuthException;
import com.mdservice.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

//    @ExceptionHandler(AuthException.class)
//    public Result handleAuthException(AuthException e) {
//        log.warn("认证失败: {}", e.getMessage());
//        return Result.error(ResultConstant.UNAUTHORIZED_CODE, e.getMessage());
//    }

    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(RuntimeException e) {
        log.error(e.getMessage(), e);
        return Result.error(e.getMessage());
    }
    /**
     * 处理参数校验异常 (Spring Validation)
     * 场景：@RequestBody 参数校验失败
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleValidationException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldError().getDefaultMessage();
        log.warn("参数校验失败: {}", msg);
        return Result.error((short) 400, msg);
    }

    /**
     * 处理空指针异常 (NullPointerException)
     * 这里的目的是为了展示可以针对特定系统异常单独处理
     */
    @ExceptionHandler(NullPointerException.class)
    public Result<?> handleNullPointerException(NullPointerException e) {
        log.error("空指针异常: ", e); // 打印堆栈信息方便排查
        return Result.error((short)500, "系统数据异常，请联系管理员");
    }

    /**
     * 4. 处理所有未知的系统异常 (兜底方案)
     * 场景：SQL报错、类型转换错误、数组越界等
     */
//    @ExceptionHandler(Exception.class)
//    public Result<?> handleException(Exception e) {
//        log.error("系统内部异常: ", e); // 务必打印堆栈！
//        return Result.error((short)500, "系统繁忙，请稍后再试");
//    }
}
