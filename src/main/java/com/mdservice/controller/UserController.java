package com.mdservice.controller;

import cn.hutool.core.util.ObjectUtil;
import com.mdservice.constant.ResultConstant;
import com.mdservice.entity.User;
import com.mdservice.service.inter.UserService;
import com.mdservice.utils.Result;
import com.mdservice.utils.UserLocal;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    @Resource
    private UserService userService;
//    注册
    @PostMapping("/register")
    public Result register(@RequestBody User user){
        //TODO 密码加密存储
        return userService.register(user);
    }
//登录
    @GetMapping("/login")
    public Result login(String account,String pwd, Byte role){
        return userService.login(account,pwd, role);
    }
//    退出
    @PutMapping("/out")
    public Result loginOut(){
        // TODO 退出登录
        return null;
    }
//    上传头像
    @PostMapping("/upload")
    public Result upload(Integer id, MultipartFile pic){
        return userService.upload(id, pic);
    }
//    修改个人信息
    @PutMapping("/modifyUser")
    public Result modify(@RequestBody User user){
        log.info("userId: {}", UserLocal.getUser());
        //根据id进行修改，id必须传
        return userService.modify(user);
    }
//    修改密码
    @PutMapping("/modifyPWD")
    public Result modifyPWD(Long id, String oldPWD, String newPWD, String newPWD2){
        // TODO 密码加密后对比
        return userService.modifyPWD(id, oldPWD, newPWD, newPWD2);
    }
    @GetMapping("/{id}")
    public Result getUserById(@PathVariable Long id){
        return userService.getUserById(id);
    }
}
