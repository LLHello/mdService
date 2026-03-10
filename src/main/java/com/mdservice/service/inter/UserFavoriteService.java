package com.mdservice.service.inter;

import com.mdservice.entity.UserFavorite;
import com.mdservice.utils.Result;

public interface UserFavoriteService {
    Result star(UserFavorite userFavorite);

    Result getStar(Long userId);

    Result unStar(Long id);

    Result getFans(Long merchantId);
}
