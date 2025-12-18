package com.mdservice.constant;

public class ResultConstant {
    public static final Short ERROR_DEFAULT_CODE = 500;
    public static final String ERROR_DEFAULT_MSG = "操作失败";
    public static final Short SUCCESS_CODE = 200;
    public static final String SUCCESS_DEFAULT_MSG = "操作成功";
    public static final Short ACCOUNT_EXISTS_CODE = 1001;
    public static final String ACCOUNT_EXISTS_MSG = "账号已经存在";
    public static final Short ACCOUNT_NOT_EXISTS_CODE = 1002;
    public static final String ACCOUNT_NOT_EXISTS_MSG = "账号不存在";
    public static final Short ACCOUNT_PWD_OR_ROLE_ISNULL_CODE = 1003;
    public static final String ACCOUNT_PWD_OR_ROLE_ISNULL_MSG = "账号、密码或角色不能为空";
    public static final Short PWD_ERROR_CODE = 1004;

    public static final String PWD_ERROR_MSG = "密码错误";
    public static final Short ROLE_ERROR_CODE = 1005;

    public static final String ROLE_ERROR_MSG = "角色不正确";
    public static final Short ACCOUNT_BAN_CODE = 1006;
    public static final String ACCOUNT_BAN_MSG = "账号封禁，请联系管理员申述";
    public static final Short ID_OR_PIC_ISNULL_CODE = 1007;

    public static final String ID_OR_PIC_ISNULL_MSG = "id或者照片不能为空";

    public static final Short ID_NOT_NULL_CODE = 1008;
    public static final String ID_NOT_NULL_MSG = "id不能为空";
    public static final Short OLD_PWD_ERROR_CODE = 1009;
    public static final String OLD_PWD_ERROR_MSG = "旧密码错误";
}
