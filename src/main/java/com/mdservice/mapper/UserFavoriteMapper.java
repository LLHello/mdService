package com.mdservice.mapper;

import com.mdservice.entity.UserFavorite;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserFavoriteMapper {
    Boolean star(UserFavorite userFavorite);

    List<UserFavorite> getStar(Long userId);

    Boolean unStar(Long id);

    Long getFans(Long merchantId);
}
