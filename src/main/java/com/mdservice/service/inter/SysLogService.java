package com.mdservice.service.inter;

import com.github.pagehelper.PageInfo;
import com.mdservice.entity.SysLog;

public interface SysLogService {
    void saveSysLog(SysLog log);

    PageInfo<SysLog> getSysLogPage(Integer pageNum, Integer pageSize, String username, String module);

    PageInfo<SysLog> getLogCost(Integer pageNum, Integer pageSize,Long cost);
}
