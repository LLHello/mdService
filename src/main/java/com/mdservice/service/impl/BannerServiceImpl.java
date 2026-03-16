package com.mdservice.service.impl;

import com.mdservice.domain.vo.BannerVO;
import com.mdservice.entity.Banner;
import com.mdservice.mapper.BannerMapper;
import com.mdservice.service.inter.BannerService;
import com.mdservice.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BannerServiceImpl implements BannerService {

    private static final int MAX_BANNER_COUNT = 3;

    @Autowired
    private BannerMapper bannerMapper;

    @Override
    public Result listActive() {
        List<BannerVO> list = bannerMapper.selectActiveBanners();
        // 只取第一张图片
        for (BannerVO vo : list) {
            vo.setPic(extractFirstPic(vo.getPic()));
        }
        return Result.success(list);
    }

    @Override
    public Result listAll() {
        List<BannerVO> list = bannerMapper.selectAllBanners();
        for (BannerVO vo : list) {
            vo.setPic(extractFirstPic(vo.getPic()));
        }
        return Result.success(list);
    }

    @Override
    public Result add(Long goodsId, Integer sort) {
        if (goodsId == null) {
            return Result.error("goodsId 不能为空");
        }
        // 限制最多3个
        int count = bannerMapper.countAll();
        if (count >= MAX_BANNER_COUNT) {
            return Result.error("轮播图最多只能设置 " + MAX_BANNER_COUNT + " 个，请先删除已有的再添加");
        }
        // 同一商品不能重复加入
        if (bannerMapper.countByGoodsId(goodsId) > 0) {
            return Result.error("该商品已在轮播图中");
        }
        Banner banner = new Banner();
        banner.setGoodsId(goodsId);
        banner.setSort(sort == null ? count : sort);
        banner.setIsActive(1);
        bannerMapper.insert(banner);
        return Result.success(banner.getId());
    }

    @Override
    public Result delete(Long id) {
        if (id == null) {
            return Result.error("id 不能为空");
        }
        bannerMapper.deleteById(id);
        return Result.success();
    }

    @Override
    public Result updateSort(Long id, Integer sort) {
        if (id == null || sort == null) {
            return Result.error("参数不能为空");
        }
        bannerMapper.updateSort(id, sort);
        return Result.success();
    }

    /** 从逗号分隔的图片字符串中取第一张 */
    private String extractFirstPic(String pic) {
        if (pic == null || pic.isBlank()) return "";
        return pic.split(",")[0].trim();
    }
}
