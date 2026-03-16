package com.mdservice.service.impl;

import com.mdservice.domain.vo.CartItemVO;
import com.mdservice.entity.CartItem;
import com.mdservice.entity.Sku;
import com.mdservice.mapper.CartMapper;
import com.mdservice.mapper.GoodsMapper;
import com.mdservice.service.inter.CartService;
import com.mdservice.utils.Result;
import com.mdservice.utils.UserLocal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    public Result add(Long skuId, Integer quantity) {
        String userIdStr = UserLocal.getUser();
        if (ObjectUtils.isEmpty(userIdStr)) {
            return Result.error("未登录");
        }
        if (skuId == null) {
            return Result.error("skuId不能为空");
        }
        if (quantity == null || quantity <= 0) {
            return Result.error("数量必须大于0");
        }
        Long userId = Long.parseLong(userIdStr);
        Sku sku = selectSku(skuId);
        if (sku == null) {
            return Result.error("SKU不存在");
        }
        CartItem existing = cartMapper.selectByUserIdAndSkuId(userId, skuId);
        if (existing == null) {
            CartItem item = new CartItem();
            item.setUserId(userId);
            item.setGoodsId(sku.getGoodsId());
            item.setSkuId(skuId);
            item.setQuantity(quantity);
            item.setChecked((byte) 1);
            cartMapper.insert(item);
            return Result.success(item.getId());
        }
        int newQty = existing.getQuantity() == null ? quantity : existing.getQuantity() + quantity;
        cartMapper.updateQuantity(existing.getId(), userId, newQty);
        return Result.success(existing.getId());
    }

    @Override
    public Result list() {
        String userIdStr = UserLocal.getUser();
        if (ObjectUtils.isEmpty(userIdStr)) {
            return Result.error("未登录");
        }
        Long userId = Long.parseLong(userIdStr);
        List<CartItemVO> list = cartMapper.selectCartByUserId(userId);
        return Result.success(list);
    }

    @Override
    public Result updateQuantity(Long cartItemId, Integer quantity) {
        String userIdStr = UserLocal.getUser();
        if (ObjectUtils.isEmpty(userIdStr)) {
            return Result.error("未登录");
        }
        if (cartItemId == null) {
            return Result.error("cartItemId不能为空");
        }
        if (quantity == null || quantity <= 0) {
            return Result.error("数量必须大于0");
        }
        Long userId = Long.parseLong(userIdStr);
        int updated = cartMapper.updateQuantity(cartItemId, userId, quantity);
        if (updated <= 0) {
            return Result.error("更新失败");
        }
        return Result.success();
    }

    @Override
    public Result updateChecked(Long cartItemId, Byte checked) {
        String userIdStr = UserLocal.getUser();
        if (ObjectUtils.isEmpty(userIdStr)) {
            return Result.error("未登录");
        }
        if (cartItemId == null) {
            return Result.error("cartItemId不能为空");
        }
        if (checked == null || (checked != 0 && checked != 1)) {
            return Result.error("checked必须为0或1");
        }
        Long userId = Long.parseLong(userIdStr);
        int updated = cartMapper.updateChecked(cartItemId, userId, checked);
        if (updated <= 0) {
            return Result.error("更新失败");
        }
        return Result.success();
    }

    @Override
    public Result remove(Long cartItemId) {
        String userIdStr = UserLocal.getUser();
        if (ObjectUtils.isEmpty(userIdStr)) {
            return Result.error("未登录");
        }
        if (cartItemId == null) {
            return Result.error("cartItemId不能为空");
        }
        Long userId = Long.parseLong(userIdStr);
        int deleted = cartMapper.deleteById(cartItemId, userId);
        if (deleted <= 0) {
            return Result.error("删除失败");
        }
        return Result.success();
    }

    private Sku selectSku(Long skuId) {
        return goodsMapper.selectSkuBySkuId(skuId);
    }
}
