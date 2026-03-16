package com.mdservice.mapper;

import com.mdservice.domain.vo.MerchantPayAmountVO;
import com.mdservice.domain.vo.ChatMerchantOrderItemVO;
import com.mdservice.entity.OrderItem;
import com.mdservice.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {
    int insertOrder(Orders order);

    int insertOrderItem(OrderItem item);

    Orders selectOrderById(@Param("id") Long id, @Param("userId") Long userId);

    List<OrderItem> selectItemsByOrderId(@Param("orderId") Long orderId);

    List<Orders> selectOrdersByUserId(@Param("userId") Long userId, @Param("status") Byte status);

    int markPaid(@Param("id") Long id, @Param("userId") Long userId);

    List<MerchantPayAmountVO> selectMerchantPayAmountByOrderId(@Param("orderId") Long orderId);

    List<ChatMerchantOrderItemVO> selectOrderItemsByUserAndMerchant(@Param("userId") Long userId, @Param("merchantId") Long merchantId);

    int markRefunded(@Param("id") Long id, @Param("userId") Long userId);

    int markCanceled(@Param("id") Long id, @Param("userId") Long userId);

    /** 记录订单使用的优惠券及实际支付金额 */
    int updateCouponInfo(@Param("id") Long id,
                         @Param("couponClaimId") Long couponClaimId,
                         @Param("discountAmount") java.math.BigDecimal discountAmount,
                         @Param("payAmount") java.math.BigDecimal payAmount);
}
