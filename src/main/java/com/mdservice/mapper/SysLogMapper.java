package com.mdservice.mapper;

import com.mdservice.entity.SysLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysLogMapper {
    /**
     * 新增操作日志
     */
    @Insert("INSERT INTO sys_operation_log (" +
            "username, module, operation, description, request_url, request_method, " +
            "method, params, time_cost, result, exception, create_time) " +
            "VALUES (" +
            "#{username}, #{module}, #{operation}, #{description}, #{requestUrl}, #{requestMethod}, " +
            "#{method}, #{params}, #{timeCost}, #{result}, #{exception}, NOW())")
    int insertSysLog(SysLog log);

    /**
     * 分页查询日志（支持按用户名、模块模糊查询）
     */
    List<SysLog> selectSysLogList(
            @Param("username") String username,
            @Param("module") String module
    );
    @Select("select * from sys_operation_log where time_cost >= #{cost}")
    List<SysLog> getLogCost(Long cost);
}
