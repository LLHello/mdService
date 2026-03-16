package com.mdservice.service.inter;

import com.mdservice.utils.Result;

public interface BannerService {

    /** 公开接口：获取首页轮播图列表（含商品跳转信息） */
    Result listActive();

    /** 管理员：获取全部轮播图 */
    Result listAll();

    /** 管理员：添加轮播图（从商品中选取） */
    Result add(Long goodsId, Integer sort);

    /** 管理员：删除轮播图 */
    Result delete(Long id);

    /** 管理员：更新排序 */
    Result updateSort(Long id, Integer sort);
}
