package com.mdservice.service.inter;

import com.mdservice.entity.User;
import com.mdservice.utils.Result;

public interface UserService {
    Result register(User user);

    Result login(String account, String pwd, Byte role);
}
