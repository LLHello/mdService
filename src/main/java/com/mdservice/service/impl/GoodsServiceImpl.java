package com.mdservice.service.impl;

import com.mdservice.constant.RedisConstant;
import com.mdservice.domain.dto.GoodsDTO;
import com.mdservice.domain.vo.GoodsVO;
import com.mdservice.entity.Category;
import com.mdservice.entity.Goods;
import com.mdservice.entity.GoodsImage;
import com.mdservice.mapper.GoodsMapper;
import com.mdservice.service.KnowledgeBaseService;
import com.mdservice.service.inter.GoodsService;
import com.mdservice.task.ClickSyncTask;
import com.mdservice.utils.FileUploadUtil;
import com.mdservice.utils.Result;
import com.mdservice.utils.UserLocal;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static com.mdservice.constant.RedisConstant.DIRTY_KEY;


@Service
@Slf4j
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private FileUploadUtil fileUploadUtil;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private KnowledgeBaseService knowledgeBaseService;
    //获取所有分类
    @Override
    public Result getCategoryList() {
        List<Category> categoryList =goodsMapper.getCategoryList();
        ArrayList<Category> collect = categoryList.stream().filter(item -> item.getIsShow() == 1).collect(Collectors.toCollection(ArrayList::new));
        return Result.success(collect);
    }
    //获取分类下的商品
    @Override
    public Result getCategoryGoods(Long id) {
        List<Goods> goods = goodsMapper.getCategoryGoods(id);
        log.info("goodsList: {}", goods);
        // 过滤能展示的商品
        ArrayList<Goods> collectGoods = goods.stream().filter(item -> item.getIsShow() == 1 && item.getStatus() == 1).collect(Collectors.toCollection(ArrayList::new));
        return Result.success(collectGoods);
    }
    //获取商品详情信息
    @Override
    public Result getMerchantGoods(Long id) {

        List<Goods> goods = goodsMapper.getMerchantGoods(id);
        return Result.success(goods);
    }
    //
    @Override
    public Result updatePics(GoodsDTO goods, List<String> deleteImagePaths, List<MultipartFile> newImages, String oldImagePath) {
        log.info("goods: {}, deleteImagePaths: {}, newImages: {}", goods, deleteImagePaths, newImages);
        log.info("oldImagePath: {}", oldImagePath);
        // 1. 更新商品基本信息
        goods.setUpdateTime(LocalDateTime.now());
        boolean updateGoodsFlag = goodsMapper.update(goods);
        if (!updateGoodsFlag) {
            return Result.error("操作失败！");
        }
        Long goodsId = goods.getId();
        log.info("goodsId: {}", goodsId);

        // 2. 处理需要删除的图片（接收文件路径列表）
        if (!CollectionUtils.isEmpty(deleteImagePaths)) {
            // 2.1 先删除本地文件（调用自定义deleteFile方法）
            for (String imagePath : deleteImagePaths) {
                log.info("deleteImagePath: {}", imagePath);
                fileUploadUtil.deleteFile(imagePath);
            }
            // 2.2 删除数据库中对应路径的图片记录
//            goodsMapper.deleteByImagePaths(deleteImagePaths);
        }
        StringBuilder sb = new StringBuilder();
        if(!ObjectUtils.isEmpty(oldImagePath)){
            sb.append(oldImagePath);
            if(!CollectionUtils.isEmpty(newImages)) sb.append(",");
        }
        // 3. 处理新上传的图片

        if (!CollectionUtils.isEmpty(newImages)) {
            for (int i = 0 ; i < newImages.size() ; i++) {
                MultipartFile file = newImages.get(i);
                try {
                    //图片已经存在，直接拼接到字符串中
                    String relativePath = file.getOriginalFilename();
                    if(!fileUploadUtil.isImageExists(relativePath)) {
                        // 3.1 上传文件，获取相对路径（如：/upload/xxx.png，或者不合法时代表是新上传的图片）
                        relativePath = fileUploadUtil.uploadFile(file);
                        log.info("新上传图片：{}", relativePath);
                        sb.append(relativePath);
                        if(i != newImages.size() - 1) {
                            sb.append(",");
                        }
                        continue;
                    }
                    // 图片已经存在，直接拼接
                    log.info("图片已存在！{}", relativePath);
                    sb.append(relativePath);
                    if(i != newImages.size() - 1) {
                        sb.append(",");
                    }
                } catch (Exception e) {
                    throw new RuntimeException("图片上传失败：" + e.getMessage());
                }
            }
        }
        String picStr = sb.toString();
        log.info("id: {}, picStr: {}", goodsId, picStr);
        goodsMapper.updatePicStr(goodsId, picStr);
        return Result.success();
    }

    @Autowired
    @Qualifier("clickSyncExecutor")
    private Executor executor;
    @Autowired
    private ClickSyncTask clickSyncTask;
    @Override
    public Result getGood(Long goodsId) {
        String userId = UserLocal.getUser();
        log.info("userId: {},goodsId: {}", userId, goodsId);
        if(userId == null){
            return Result.error("用户id为null!");
        }

        //查看该用户是否点击过
        Boolean member = redisTemplate.opsForSet().isMember(RedisConstant.CLICK_USER_KEY+Long.parseLong(userId), String.valueOf(goodsId));
        if(!member){
            //加入集合，防止重复点击计数
            //goods:click:userId
            redisTemplate.opsForSet().add(RedisConstant.CLICK_USER_KEY+Long.parseLong(userId), String.valueOf(goodsId));
            //将当前商品点击数+1
            redisTemplate.opsForZSet().incrementScore(RedisConstant.RANK_KEY, String.valueOf(goodsId), 1);
            //将变化的放入集合，方便定时更新
            redisTemplate.opsForSet().add(DIRTY_KEY, String.valueOf(goodsId));
            // 如果脏数据积压超过 10 条，立即触发一次异步同步
            Long size = redisTemplate.opsForSet().size(DIRTY_KEY);
            if (size != null && size > 10) {
                clickSyncTask.syncToDb();
            }
        }

        Goods good = goodsMapper.getGood(goodsId);
        log.info("good: {}", good);
        return Result.success(good);
    }
    //top15商品
    @Override
    public Result getTop10() {
        Set<String> top10 = redisTemplate.opsForZSet().reverseRange(RedisConstant.RANK_KEY, 0, 9);
        List<GoodsVO> Top10Goods = goodsMapper.getGoods(top10);
        return Result.success(Top10Goods);
    }
    //获取商品点击量
    @Override
    public Result getGoodsClickCount(Long goodId) {
        Double score = redisTemplate.opsForZSet().score(RedisConstant.RANK_KEY, String.valueOf(goodId));
        return Result.success(score);
    }


    public Result addGood(GoodsDTO goodsDTO, List<MultipartFile> images) {
        StringBuilder sb = new StringBuilder();
        //上传图片,并记录路径
        for(int i = 0 ; i < images.size() ; i++) {
            MultipartFile file = images.get(i);
            try {
                String s = fileUploadUtil.uploadFile(file);
                sb.append(s);
                if(i != images.size() - 1) {
                    sb.append(",");
                }
            } catch (IOException e) {
                log.error("上传商品图片失败：{}", e.getMessage());
                throw new RuntimeException(e);
            }
        }
        String picStr = sb.toString();
        //拷贝，然后更新数据库
        Goods goods = new Goods();
        BeanUtils.copyProperties(goodsDTO, goods);
        goods.setPic(picStr);
        goods.setUpdateTime(LocalDateTime.now());
        goods.setCreateTime(LocalDateTime.now());
        goods.setClickTimes(0L);
        Long b = goodsMapper.addGood(goods);
        if (b == null) {
            return Result.error("添加商品失败！");
        }
        log.info("商品主键：{}", goods.getId());
        String s = goods.getId() + "," + goods.getTitle()+ "," + goods.getPrice() + "," + goods.getDes();
        knowledgeBaseService.importKnowledge(s);
        return Result.success();
    }

    @Override
    public Result modifyStatus(Long goodsId, Integer status) {
        Boolean b = goodsMapper.modifyStatus(goodsId, status);
        if (!b) {
            return Result.error();
        }
        return Result.success();
    }

    @Override
    public Result categoryIdAdmin(Long id) {
        List<Goods> goodsList = goodsMapper.categoryIdAdmin(id);
        return Result.success(goodsList);
    }

    @Override
    public List<Goods> findByName(String name) {
        return goodsMapper.findByName(name);
    }

}
