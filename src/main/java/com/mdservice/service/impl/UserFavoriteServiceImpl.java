package com.mdservice.service.impl;

import com.mdservice.entity.UserFavorite;
import com.mdservice.mapper.UserFavoriteMapper;
import com.mdservice.service.inter.UserFavoriteService;
import com.mdservice.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserFavoriteServiceImpl implements UserFavoriteService {
    @Autowired
    private UserFavoriteMapper userFavoriteMapper;

    @Override
    public Result star(UserFavorite userFavorite) {
        userFavorite.setCreateTime(LocalDateTime.now());
        Boolean b = userFavoriteMapper.star(userFavorite);
        if(!b){
            return Result.error();
        }
        return Result.success();
    }

    @Override
    public Result getStar(Long userId) {
        List<UserFavorite> userFavorites =  userFavoriteMapper.getStar(userId);
        return Result.success(userFavorites);
    }

    @Override
    public Result unStar(Long id) {
        Boolean b = userFavoriteMapper.unStar(id);
        if(!b){
            return Result.error();
        }
        return Result.success();
    }

    @Override
    public Result getFans(Long merchantId) {
        Long count = userFavoriteMapper.getFans(merchantId);
        return Result.success(count);
    }
}
