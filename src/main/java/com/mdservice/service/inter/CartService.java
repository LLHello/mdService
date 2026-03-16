package com.mdservice.service.inter;

import com.mdservice.utils.Result;

public interface CartService {
    Result add(Long skuId, Integer quantity);

    Result list();

    Result updateQuantity(Long cartItemId, Integer quantity);

    Result updateChecked(Long cartItemId, Byte checked);

    Result remove(Long cartItemId);
}

