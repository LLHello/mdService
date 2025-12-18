package com.mdservice.service.inter;

import com.mdservice.entity.User;
import com.mdservice.utils.Result;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    Result register(User user);

    Result login(String account, String pwd, Byte role);

    Result upload(Integer id, MultipartFile pic);

    Result modify(User user);

    Result modifyPWD(Long id, String oldPWD, String newPWD, String newPWD2);
}
