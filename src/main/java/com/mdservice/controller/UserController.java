package com.mdservice.controller.userController;

import cn.hutool.core.util.ObjectUtil;
import com.mdservice.constant.ResultConstant;
import com.mdservice.entity.User;
import com.mdservice.service.inter.UserService;
import com.mdservice.utils.Result;
import jakarta.annotation.Resource;
import jakarta.servlet.annotation.MultipartConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    @Resource
    private UserService userService;

    @PostMapping("/register")
    public Result register(@RequestBody User user){
        return userService.register(user);
    }

    @GetMapping("/login")
    public Result login(String account,String pwd, Byte role){
        return userService.login(account,pwd, role);
    }
    @PutMapping("/out")
    public Result loginOut(){
        // TODO 退出登录
        return null;
    }
    @PostMapping("upload")
    public Result upload(Integer id, MultipartFile pic){

        return userService.upload(id, pic);
    }
}
