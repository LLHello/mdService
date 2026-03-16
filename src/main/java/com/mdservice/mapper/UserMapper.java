package com.mdservice.mapper;

import com.mdservice.domain.vo.UserVO;
import com.mdservice.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface UserMapper {
    Long queryAccount(String account);

    void registerAccount(User user);

    User queryUser(String account);

    User queryId(Integer id);

    Long updateIcon(User user);

    void updateUser(User user);

    User queryById(Long id);

    void updatePWD(Long id, String newPWD);

    List<UserVO> getAll();

    Boolean modifyShow(Byte isShow, Long userId);

    int deductMoney(Long userId, BigDecimal amount);

    int addMoney(Long userId, BigDecimal amount);
}
