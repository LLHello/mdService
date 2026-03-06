package com.mdservice.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mdservice.entity.SysLog;
import com.mdservice.mapper.SysLogMapper;
import com.mdservice.service.inter.SysLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysLogServiceImpl implements SysLogService {
    @Resource
    private SysLogMapper sysLogMapper;

    /**
     * 保存操作日志
     */
    public void saveSysLog(SysLog log) {
        sysLogMapper.insertSysLog(log);
    }

    /**
     * 分页查询日志
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页条数
     * @param username 用户名（模糊查询）
     * @param module 模块（模糊查询）
     * @return 分页结果
     */
    public PageInfo<SysLog> getSysLogPage(
            Integer pageNum,
            Integer pageSize,
            String username,
            String module) {
        // 开启分页
        PageHelper.startPage(pageNum, pageSize);
        // 查询数据
        List<SysLog> logList = sysLogMapper.selectSysLogList(username, module);
        // 封装分页结果
        return new PageInfo<>(logList);
    }
    //查询执行耗时大于等于cost的操作
    @Override
    public PageInfo<SysLog> getLogCost(Integer pageNum, Integer pageSize,Long cost) {
        PageHelper.startPage(pageNum, pageSize);
        List<SysLog> logList = sysLogMapper.getLogCost(cost);
        return new PageInfo<>(logList);
    }
}
