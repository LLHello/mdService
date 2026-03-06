package com.mdservice.aop;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    /** 操作模块（如：用户管理、订单管理） */
    String module() default "";

    /** 操作类型（如：新增、修改、删除、查询） */
    String operation() default "";

    /** 操作描述（可选） */
    String desc() default "";
}
