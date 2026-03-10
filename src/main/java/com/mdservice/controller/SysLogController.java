package com.mdservice.controller;

import com.github.pagehelper.PageInfo;
import com.mdservice.aop.Log;
import com.mdservice.entity.SysLog;
import com.mdservice.service.inter.SysLogService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sys/log")
public class SysLogController {
    @Resource
    private SysLogService sysLogService;

    /**
     * 分页查询操作日志
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页条数
     * @param username 用户名（模糊查询，可选）
     * @param module 模块（模糊查询，可选）
     * @return 分页日志列表
     */
    @Log(module = "系统日志模块", operation = "查看系统日志", desc = "查看所有系统日志")
    @GetMapping("/list")
    public PageInfo<SysLog> getLogList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String module) {
        // 处理空参数
        username = username == null ? "" : username;
        module = module == null ? "" : module;
        // 调用 Service 分页查询
        return sysLogService.getSysLogPage(pageNum, pageSize, username, module);
    }
    //查询执行耗时大于等于cost的操作
    @Log(module = "系统日志", operation = "日志耗时", desc = "搜索日志耗时大于等于某个值的数据")
    @GetMapping("/getCost/{cost}")
    public PageInfo<SysLog> getLogCost(@RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize,@PathVariable Long cost) {
        return sysLogService.getLogCost(pageNum, pageSize,cost);
    }
}
