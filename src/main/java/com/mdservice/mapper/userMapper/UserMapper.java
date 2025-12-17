package com.mdservice.mapper.userMapper;

import com.mdservice.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    Long queryAccount(String account);

    void registerAccount(User user);

    User queryUser(String account);
}
