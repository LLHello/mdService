package com.mdservice.mapper;

import com.mdservice.domain.vo.CartItemVO;
import com.mdservice.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CartMapper {
    CartItem selectByUserIdAndSkuId(@Param("userId") Long userId, @Param("skuId") Long skuId);

    int insert(CartItem item);

    int updateQuantity(@Param("id") Long id, @Param("userId") Long userId, @Param("quantity") Integer quantity);

    int updateChecked(@Param("id") Long id, @Param("userId") Long userId, @Param("checked") Byte checked);

    int deleteById(@Param("id") Long id, @Param("userId") Long userId);

    List<CartItemVO> selectCartByUserId(@Param("userId") Long userId);

    List<CartItemVO> selectCheckedCartByUserId(@Param("userId") Long userId);

    int deleteCheckedByUserId(@Param("userId") Long userId);
}

