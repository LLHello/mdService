package com.mdservice.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.mdservice.constant.ResultConstant;
import com.mdservice.entity.User;
import com.mdservice.mapper.UserMapper;
import com.mdservice.service.inter.UserService;
import com.mdservice.utils.FileUploadUtil;
import com.mdservice.utils.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Resource
    private FileUploadUtil fileUploadUtil;
    @Resource
    private UserMapper userMapper;

    /*
    * 注册方法
    *
    * */
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

        //默认启用
        user.setIsShow((byte) 1);
        //创建时间
        user.setCreateTime(LocalDateTime.now());
        //创建账号
        userMapper.registerAccount(user);
        log.info("注册成功");
        return Result.success();
    }

/*
* 登录方法
* */
    @Override
    public Result login(String account, String pwd, Byte role) {
        if("".equals(account) || "".equals(pwd) || ObjectUtil.isNull(role)){
            return Result.error(ResultConstant.ACCOUNT_PWD_OR_ROLE_ISNULL_CODE,ResultConstant.ACCOUNT_PWD_OR_ROLE_ISNULL_MSG);
        }
        //查询是否存在账户
        User user = userMapper.queryUser(account);
        if(ObjectUtil.isNull(user)){
            log.error("不存在用户!");
            return Result.error(ResultConstant.ACCOUNT_NOT_EXISTS_CODE,ResultConstant.ACCOUNT_NOT_EXISTS_MSG);
        }
        //判断密码
        if(!user.getPassword().equals(pwd)){
            log.error("密码错误！");
            return Result.error(ResultConstant.PWD_ERROR_CODE, ResultConstant.PWD_ERROR_MSG);
        }
        //判断登录选择的角色和实际角色
        if(!user.getRole().equals(role)){
            log.error("角色不正确！");
            return Result.error(ResultConstant.ROLE_ERROR_CODE, ResultConstant.ROLE_ERROR_MSG);
        }
        //判断是否被封禁
        if(user.getIsShow() == 0){
            return Result.error(ResultConstant.ACCOUNT_BAN_CODE, ResultConstant.ACCOUNT_BAN_MSG);
        }
        //隐藏密码
        user.setPassword("");
        log.info("user: {}", user);
        return Result.success(user);
    }
//上传用户头像
    @Override
    public Result upload(Integer id, MultipartFile pic) {
        //判断前端传入的id和图片是否为null
        if(ObjectUtil.isNull(id) || ObjectUtil.isNull(pic)){
            return Result.error(ResultConstant.ID_OR_PIC_ISNULL_CODE, ResultConstant.ID_OR_PIC_ISNULL_MSG);
        }
        //TODO 配置全局异常处理器后，改为直接方法上抛出，进行统一处理
        String fileName;
        try {
            fileName = fileUploadUtil.uploadFile(pic);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //查询用户
        User user = userMapper.queryId(id);
        //删除原有文件
        Boolean aBoolean = fileUploadUtil.deleteFile(user.getIcon());
        log.info("删除文件：" + (aBoolean ? "成功" : "失败")+"; 文件路径：{}", user.getIcon());
        //设置新的文件路径
        user.setIcon(fileName);
        //更新文件路径
        Long count = userMapper.updateIcon(user);
        log.info("返回给前端的路径：{}", fileName);
        log.info("更新影响的数据条数：{}", count);
        return Result.success(fileName);
    }
//修改个人信息
    @Override
    public Result modify(User user) {
        if(ObjectUtil.isNull(user.getId())){
            log.info("id不能为空");
            return Result.error(ResultConstant.ID_NOT_NULL_CODE, ResultConstant.ID_NOT_NULL_MSG);
        }
        //设置更新时间
        //TODO 后期使用AOP统一处理
        user.setUpdateTime(LocalDateTime.now());
        //更新数据，注意有些不能更新
        userMapper.updateUser(user);
        //查询更新后的数据，做处理后返回
        user = userMapper.queryById(user.getId());
        user.setPassword("");
        return Result.success(user);
    }
/*
* 修改密码
* */
    @Override
    public Result modifyPWD(Long id, String oldPWD, String newPWD, String newPWD2) {
        if("".equals(newPWD) || "".equals(newPWD2)){
            return Result.error("请输入新密码");
        }
        if(!newPWD.equals(newPWD2)){
            return Result.error("新密码两次输入不一致");
        }
        User user = userMapper.queryById(id);
        if(!user.getPassword().equals(oldPWD)){
            return Result.error(ResultConstant.OLD_PWD_ERROR_CODE, ResultConstant.OLD_PWD_ERROR_MSG);
        }
        userMapper.updatePWD(id, newPWD);
        return Result.success("更改成功，请重新登录");
    }
}
