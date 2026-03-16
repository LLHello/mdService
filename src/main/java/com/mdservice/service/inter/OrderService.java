package com.mdservice.service.inter;

import com.mdservice.utils.Result;

public interface OrderService {
    Result createFromCart(Long couponClaimId);

    Result list(Byte status);

    Result detail(Long orderId);

    Result pay(Long orderId);

    Result refund(Long orderId);

    Result cancel(Long orderId);
}
