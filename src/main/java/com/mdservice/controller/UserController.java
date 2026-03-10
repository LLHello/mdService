package com.mdservice.controller;

import cn.hutool.core.util.ObjectUtil;
import com.mdservice.aop.Log;
import com.mdservice.constant.ResultConstant;
import com.mdservice.entity.User;
import com.mdservice.mapper.FeedbackMapper;
import com.mdservice.mapper.UserMapper;
import com.mdservice.service.inter.FeedbackService;
import com.mdservice.service.inter.UserService;
import com.mdservice.utils.Result;
import com.mdservice.utils.UserLocal;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    @Resource
    private UserService userService;
    @Autowired
    private FeedbackService feedbackService;
//    注册
    @Log(module = "用户模块", operation = "注册", desc = "用户和商家注册")
    @PostMapping("/register")
    public Result register(@RequestBody User user){
        //TODO 密码加密存储
        return userService.register(user);
    }
//登录
    @Log(module = "用户模块", operation = "登录", desc = "登录")
    @GetMapping("/login")
    public Result login(String account,String pwd, Byte role){
        return userService.login(account,pwd, role);
    }
//    退出
    @Log(module = "用户模块", operation = "退出", desc = "用户退出")
    @PutMapping("/out")
    public Result loginOut(){
        // TODO 退出登录
        return null;
    }
//    上传头像
    @Log(module = "用户模块", operation = "上传头像", desc = "用户上传头像")
    @PostMapping("/upload")
    public Result upload(Integer id, MultipartFile pic){
        return userService.upload(id, pic);
    }
//    修改个人信息
    @Log(module = "用户模块", operation = "修改信息", desc = "用户修改个人信息")
    @PutMapping("/modifyUser")
    public Result modify(@RequestBody User user){
        log.info("userId: {}", UserLocal.getUser());
        //根据id进行修改，id必须传
        return userService.modify(user);
    }
//    修改密码
    @Log(module = "用户模块", operation = "修改密码", desc = "用户修改密码")
    @PutMapping("/modifyPWD")
    public Result modifyPWD(Long id, String oldPWD, String newPWD, String newPWD2){
        // TODO 密码加密后对比
        return userService.modifyPWD(id, oldPWD, newPWD, newPWD2);
    }
    @Log(module = "用户模块", operation = "获取用户", desc = "根据id获取用户信息")
    @GetMapping("/{id}")
    public Result getUserById(@PathVariable Long id){
        return userService.getUserById(id);
    }
    //获取所有用户
    @Log(module = "用户模块", operation = "获取所有用户", desc = "管理员获取所有用户信息")
    @GetMapping("/getAll")
    public Result getAllUsers(){
        return userService.getAll();
    }
    //管理员修改用户是否封禁，0封禁，1启用
    @Log(module = "用户模块", operation = "管理用户", desc = "管理员管理是否封禁用户")
    @PutMapping("/modifyShow")
    public Result modifyShow(Byte isShow, Long userId){
        return userService.modifyShow(isShow, userId);
    }
}
