package com.mdservice.entity;

import lombok.Data;

import java.util.Date;

@Data
public class SysLog {
    private Long id;             // 主键ID
    private String username;     // 操作用户名
    private String module;       // 操作模块
    private String operation;    // 操作类型
    private String description;  // 操作描述
    private String requestUrl;   // 请求URL
    private String requestMethod;// 请求方式（GET/POST）
    private String method;       // 方法全路径
    private String params;       // 请求参数
    private Long timeCost;       // 操作耗时（毫秒）
    private String result;       // 操作结果（成功/失败）
    private String exception;    // 异常信息
    private Date createTime;     // 操作时间
}
