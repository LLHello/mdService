package com.mdservice.service.impl;

import com.mdservice.entity.OfflineMessage;
import com.mdservice.entity.SystemNotice;
import com.mdservice.mapper.OfflineMessageMapper;
import com.mdservice.mapper.SystemNoticeMapper;
import com.mdservice.service.inter.NoticeService;
import com.mdservice.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class NoticeServiceImpl implements NoticeService {
    @Autowired
    private SystemNoticeMapper systemNoticeMapper;
    @Autowired
    private OfflineMessageMapper offlineMessageMapper;
//    管理员删除系统公告消息
    @Override
    public Result deleteSystemNotice(String id) {
        Boolean b = systemNoticeMapper.deleteSysteNotice(id);
        if (!b) {
            return Result.error();
        }
        return Result.success();
    }
//  管理员修改系统公告消息
    @Override
    public Result modifySystemNotice(SystemNotice systemNotice) {
        Boolean b = systemNoticeMapper.modifySystemNotice(systemNotice);
        if(!b){
            return Result.error();
        }
        return Result.success();
    }
//  获取所有系统公告消息
    @Override
    public Result getAllSystemNotice() {
        List<SystemNotice> res = systemNoticeMapper.getAllSystemNotice();
        log.info("系统公告内容：{}", res);
        return Result.success(res);
    }
// 获取系统发给当前用户的消息
    @Override
    public Result getAllSystemToUser(String userId) {
        List<OfflineMessage>  res = offlineMessageMapper.getAllSystemToUser(userId);
        return Result.success(res);
    }
}
