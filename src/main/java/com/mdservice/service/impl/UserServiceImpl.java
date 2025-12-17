package com.mdservice.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.mdservice.constant.ResultConstant;
import com.mdservice.entity.User;
import com.mdservice.exception.AccountException;
import com.mdservice.mapper.userMapper.UserMapper;
import com.mdservice.service.inter.UserService;
import com.mdservice.utils.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserMapper userMapper;
    @Override
    public Result register(User user) {
        //查询数据库中是否有此账户
        Long count = userMapper.queryAccount(user.getAccount());
        log.info("账号数量：{}", count);
        if(count > 0){
            return Result.error(ResultConstant.ACCOUNT_EXISTS_CODE, ResultConstant.ACCOUNT_EXISTS_MSG);
        }
        // TODO 使用redis分布式锁，避免高并发下，创建相同账号；并且使用缓存避免一段时间内，相同账号注册全打入mysql
        // TODO 注销时需要检查锁和缓存是否存在，避免mysql中没有账户，却无法创建的情况
        //创建账号
        //默认启用
        user.setIsShow((byte) 1);
        userMapper.registerAccount(user);
        return Result.success();
    }

    @Override
    public Result login(String account, String pwd, Byte role) {
        User user = userMapper.queryUser(account);
        if(ObjectUtil.isNull(user)){
            log.error("不存在用户!");
            return Result.error(ResultConstant.ACCOUNT_NOT_EXISTS_CODE,ResultConstant.ACCOUNT_NOT_EXISTS_MSG);
        }
        if(!user.getPassword().equals(pwd)){
            log.error("密码错误！");
            return Result.error(ResultConstant.PWD_ERROR_CODE, ResultConstant.PWD_ERROR_MSG);
        }
        if(!user.getRole().equals(role)){
            log.error("角色不正确！");
            return Result.error(ResultConstant.ROLE_ERROR_CODE, ResultConstant.ROLE_ERROR_MSG);
        }
        if(user.getIsShow() == 0){
            return Result.error(ResultConstant.ACCOUNT_BAN_CODE, ResultConstant.ACCOUNT_BAN_MSG);
        }
        //隐藏密码
        user.setPassword("");
        log.info("user: {}", user);
        return Result.success(user);
    }
}
