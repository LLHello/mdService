package com.mdservice.service.impl;

import com.mdservice.domain.vo.CartItemVO;
import com.mdservice.domain.vo.MerchantPayAmountVO;
import com.mdservice.domain.vo.OrderDetailVO;
import com.mdservice.domain.vo.OrderItemVO;
import com.mdservice.entity.CouponClaim;
import com.mdservice.entity.OrderItem;
import com.mdservice.entity.Orders;
import com.mdservice.mapper.CartMapper;
import com.mdservice.mapper.CouponMapper;
import com.mdservice.mapper.GoodsMapper;
import com.mdservice.mapper.UserMapper;
import com.mdservice.mapper.OrderMapper;
import com.mdservice.service.inter.OrderService;
import com.mdservice.utils.Result;
import com.mdservice.utils.UserLocal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private CouponMapper couponMapper;

    @Override
    @Transactional
    public Result createFromCart(Long couponClaimId) {
        String userIdStr = UserLocal.getUser();
        if (ObjectUtils.isEmpty(userIdStr)) {
            return Result.error("未登录");
        }
        Long userId = Long.parseLong(userIdStr);
        List<CartItemVO> items = cartMapper.selectCheckedCartByUserId(userId);
        if (items == null || items.isEmpty()) {
            return Result.error("购物车没有选中商品");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (CartItemVO item : items) {
            if (item.getPrice() == null || item.getQuantity() == null) {
                return Result.error("购物车数据异常");
            }
            total = total.add(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
        }

        // 验证优惠券
        BigDecimal discount = BigDecimal.ZERO;
        Long validClaimId = null;
        if (couponClaimId != null) {
            CouponClaim claim = couponMapper.selectClaimById(couponClaimId);
            if (claim == null || !userId.equals(claim.getUserId())) {
                return Result.error("优惠券不存在");
            }
            if (claim.getStatus() != 0 || claim.getUsedOrderId() != null) {
                return Result.error("优惠券已使用");
            }
            com.mdservice.entity.Coupon coupon = couponMapper.selectById(claim.getCouponId());
            if (coupon == null || coupon.getStatus() == null || coupon.getStatus() != 1) {
                return Result.error("优惠券不可用");
            }
            if (coupon.getEndTime() != null && coupon.getEndTime().isBefore(LocalDateTime.now())) {
                return Result.error("优惠券已过期");
            }
            // 专属商品优惠券：购物车中必须包含该商品
            if (coupon.getGoodsId() != null) {
                boolean hasGoods = items.stream()
                        .anyMatch(it -> coupon.getGoodsId().equals(it.getGoodsId()));
                if (!hasGoods) {
                    return Result.error("该优惠券仅限指定商品使用，购物车中未包含该商品");
                }
            }
            if (coupon.getThresholdAmount() != null
                    && total.compareTo(coupon.getThresholdAmount()) < 0) {
                return Result.error("订单金额未达到优惠券使用门槛 ¥" + coupon.getThresholdAmount());
            }
            discount = coupon.getDiscountAmount() != null ? coupon.getDiscountAmount() : BigDecimal.ZERO;
            if (discount.compareTo(total) > 0) discount = total;
            validClaimId = couponClaimId;
        }

        BigDecimal payAmount = total.subtract(discount);

        Orders order = new Orders();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setStatus((byte) 0);
        order.setTotalAmount(total);
        order.setPayAmount(payAmount);
        order.setDiscountAmount(discount);
        order.setCouponClaimId(validClaimId);
        order.setCreateTime(LocalDateTime.now());
        orderMapper.insertOrder(order);

        // 若使用了优惠券，立刻锁定（状态改为已使用，防止重复使用）
        if (validClaimId != null) {
            couponMapper.markClaimUsed(validClaimId, order.getId());
        }

        for (CartItemVO cartItem : items) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setGoodsId(cartItem.getGoodsId());
            orderItem.setSkuId(cartItem.getSkuId());
            orderItem.setTitle(cartItem.getTitle());
            orderItem.setPic(cartItem.getPic());
            orderItem.setPrice(cartItem.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalAmount(cartItem.getPrice().multiply(new BigDecimal(cartItem.getQuantity())));
            orderItem.setCreateTime(LocalDateTime.now());
            orderMapper.insertOrderItem(orderItem);
        }

        cartMapper.deleteCheckedByUserId(userId);
        return Result.success(order.getId());
    }

    @Override
    public Result list(Byte status) {
        String userIdStr = UserLocal.getUser();
        if (ObjectUtils.isEmpty(userIdStr)) {
            return Result.error("未登录");
        }
        Long userId = Long.parseLong(userIdStr);
        List<Orders> orders = orderMapper.selectOrdersByUserId(userId, status);
        return Result.success(orders);
    }

    @Override
    public Result detail(Long orderId) {
        String userIdStr = UserLocal.getUser();
        if (ObjectUtils.isEmpty(userIdStr)) {
            return Result.error("未登录");
        }
        if (orderId == null) {
            return Result.error("orderId不能为空");
        }
        Long userId = Long.parseLong(userIdStr);
        Orders order = orderMapper.selectOrderById(orderId, userId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        List<OrderItem> items = orderMapper.selectItemsByOrderId(orderId);

        OrderDetailVO vo = new OrderDetailVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setStatus(order.getStatus());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setPayAmount(order.getPayAmount());
        vo.setCouponClaimId(order.getCouponClaimId());
        vo.setDiscountAmount(order.getDiscountAmount());
        vo.setCreateTime(order.getCreateTime());
        vo.setPayTime(order.getPayTime());
        vo.setUpdateTime(order.getUpdateTime());

        List<OrderItemVO> itemVos = new ArrayList<>();
        if (items != null) {
            for (OrderItem item : items) {
                OrderItemVO itemVO = new OrderItemVO();
                itemVO.setId(item.getId());
                itemVO.setGoodsId(item.getGoodsId());
                itemVO.setSkuId(item.getSkuId());
                itemVO.setTitle(item.getTitle());
                itemVO.setPic(item.getPic());
                itemVO.setPrice(item.getPrice());
                itemVO.setQuantity(item.getQuantity());
                itemVO.setTotalAmount(item.getTotalAmount());
                itemVos.add(itemVO);
            }
        }
        vo.setItems(itemVos);
        return Result.success(vo);
    }

    @Override
    @Transactional
    public Result pay(Long orderId) {
        String userIdStr = UserLocal.getUser();
        if (ObjectUtils.isEmpty(userIdStr)) {
            return Result.error("未登录");
        }
        if (orderId == null) {
            return Result.error("orderId不能为空");
        }
        Long userId = Long.parseLong(userIdStr);
        Orders order = orderMapper.selectOrderById(orderId, userId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (order.getStatus() == null || order.getStatus() != 0) {
            return Result.error("订单状态不允许支付");
        }
        if (order.getPayAmount() == null || order.getPayAmount().compareTo(BigDecimal.ZERO) < 0) {
            return Result.error("订单金额异常");
        }

        int updated = orderMapper.markPaid(orderId, userId);
        if (updated == 0) {
            return Result.error("订单已支付或状态已变更");
        }

        List<OrderItem> items = orderMapper.selectItemsByOrderId(orderId);
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("订单明细为空");
        }
        Map<Long, Integer> skuQuantityMap = new HashMap<>();
        for (OrderItem item : items) {
            if (item == null || item.getSkuId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new RuntimeException("订单明细异常");
            }
            skuQuantityMap.merge(item.getSkuId(), item.getQuantity(), Integer::sum);
        }
        for (Map.Entry<Long, Integer> entry : skuQuantityMap.entrySet()) {
            int stockUpdated = goodsMapper.deductSkuStock(entry.getKey(), entry.getValue());
            if (stockUpdated == 0) {
                throw new RuntimeException("库存不足");
            }
        }

        int moneyUpdated = userMapper.deductMoney(userId, order.getPayAmount());
        if (moneyUpdated == 0) {
            throw new RuntimeException("余额不足");
        }
        List<MerchantPayAmountVO> merchantPayAmounts = orderMapper.selectMerchantPayAmountByOrderId(orderId);
        if (merchantPayAmounts == null || merchantPayAmounts.isEmpty()) {
            throw new RuntimeException("订单商家信息缺失");
        }
        for (MerchantPayAmountVO item : merchantPayAmounts) {
            if (item == null || item.getMerchantId() == null || item.getAmount() == null) {
                throw new RuntimeException("订单商家金额异常");
            }
            if (item.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("订单商家金额异常");
            }
            int creditUpdated = userMapper.addMoney(item.getMerchantId(), item.getAmount());
            if (creditUpdated == 0) {
                throw new RuntimeException("商家入账失败");
            }
        }
        return Result.success();
    }

    @Override
    @Transactional
    public Result refund(Long orderId) {
        String userIdStr = UserLocal.getUser();
        if (ObjectUtils.isEmpty(userIdStr)) {
            return Result.error("未登录");
        }
        if (orderId == null) {
            return Result.error("orderId不能为空");
        }
        Long userId = Long.parseLong(userIdStr);
        Orders order = orderMapper.selectOrderById(orderId, userId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (order.getStatus() == null || order.getStatus() != 1) {
            return Result.error("订单状态不允许退款");
        }
        if (order.getPayAmount() == null || order.getPayAmount().compareTo(BigDecimal.ZERO) < 0) {
            return Result.error("订单金额异常");
        }

        int updated = orderMapper.markRefunded(orderId, userId);
        if (updated == 0) {
            return Result.error("订单已退款或状态已变更");
        }

        // 退款时归还优惠券
        if (order.getCouponClaimId() != null) {
            couponMapper.markClaimUnused(order.getCouponClaimId());
        }

        List<OrderItem> items = orderMapper.selectItemsByOrderId(orderId);
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("订单明细为空");
        }
        Map<Long, Integer> skuQuantityMap = new HashMap<>();
        for (OrderItem item : items) {
            if (item == null || item.getSkuId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new RuntimeException("订单明细异常");
            }
            skuQuantityMap.merge(item.getSkuId(), item.getQuantity(), Integer::sum);
        }
        for (Map.Entry<Long, Integer> entry : skuQuantityMap.entrySet()) {
            int stockUpdated = goodsMapper.addSkuStock(entry.getKey(), entry.getValue());
            if (stockUpdated == 0) {
                throw new RuntimeException("退回库存失败");
            }
        }

        List<MerchantPayAmountVO> merchantPayAmounts = orderMapper.selectMerchantPayAmountByOrderId(orderId);
        if (merchantPayAmounts == null || merchantPayAmounts.isEmpty()) {
            throw new RuntimeException("订单商家信息缺失");
        }
        for (MerchantPayAmountVO item : merchantPayAmounts) {
            if (item == null || item.getMerchantId() == null || item.getAmount() == null) {
                throw new RuntimeException("订单商家金额异常");
            }
            if (item.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("订单商家金额异常");
            }
            int debitUpdated = userMapper.deductMoney(item.getMerchantId(), item.getAmount());
            if (debitUpdated == 0) {
                throw new RuntimeException("商家余额不足，退款失败");
            }
        }

        int creditUpdated = userMapper.addMoney(userId, order.getPayAmount());
        if (creditUpdated == 0) {
            throw new RuntimeException("退款入账失败");
        }
        return Result.success();
    }

    @Override
    @Transactional
    public Result cancel(Long orderId) {
        String userIdStr = UserLocal.getUser();
        if (ObjectUtils.isEmpty(userIdStr)) {
            return Result.error("未登录");
        }
        if (orderId == null) {
            return Result.error("orderId不能为空");
        }
        Long userId = Long.parseLong(userIdStr);
        Orders order = orderMapper.selectOrderById(orderId, userId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (order.getStatus() == null || order.getStatus() != 0) {
            return Result.error("订单状态不允许取消");
        }
        int updated = orderMapper.markCanceled(orderId, userId);
        if (updated == 0) {
            return Result.error("订单已取消或状态已变更");
        }
        return Result.success();
    }

    private String generateOrderNo() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
