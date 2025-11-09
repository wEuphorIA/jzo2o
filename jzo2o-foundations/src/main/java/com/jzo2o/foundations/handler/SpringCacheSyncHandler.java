package com.jzo2o.foundations.handler;

import com.jzo2o.api.foundations.dto.response.RegionSimpleResDTO;
import com.jzo2o.foundations.constants.RedisConstants;

import com.jzo2o.foundations.model.domain.Serve;
import com.jzo2o.foundations.service.HomeService;
import com.jzo2o.foundations.service.IRegionService;
import com.jzo2o.foundations.service.IServeService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * springCache缓存同步任务
 *
 * @author itcast
 * @create 2023/8/15 18:14
 **/
@Slf4j
@Component
public class SpringCacheSyncHandler {

    @Resource
    private IRegionService regionService;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private HomeService homeService;
    @Resource
    private IServeService serveService;

    /**
     * 已启用区域缓存更新
     * 每日凌晨1点执行
     */
    @XxlJob(value = "activeRegionCacheSync")
    public void activeRegionCacheSync() {
        log.info(">>>>>>>>开始进行缓存同步，更新已启用区域");
        //1.清理缓存
        String key = RedisConstants.CacheName.JZ_CACHE + "::ACTIVE_REGIONS";
        redisTemplate.delete(key);

        log.info(">>>>>>>>开始进行缓存同步，更新已启用区域");

        //删除缓存
        Boolean delete = redisTemplate.delete(RedisConstants.CacheName.JZ_CACHE + "::ACTIVE_REGIONS");

        //通过查询开通区域列表进行缓存
        List<RegionSimpleResDTO> regionSimpleResDTOS = regionService.queryActiveRegionList();

        //遍历区域对该区域下的服务类型进行缓存
        regionSimpleResDTOS.forEach(item->{
            //区域id
            Long regionId = item.getId();

            //删除该区域下的首页服务列表
            String serve_type_key = RedisConstants.CacheName.SERVE_ICON + "::" + regionId;
            redisTemplate.delete(serve_type_key);
            homeService.queryServeIconCategoryByRegionIdCache(regionId);
        });
    }


    /**
     * 用户端首页所选城市服务缓存更新
     * 每日凌晨1点执行
     */
    @XxlJob(value = "cityServeCacheSync")
    public void cityServeCacheSync() {
        log.info(">>>>>>>>开始进行缓存同步，更新用户端首页所选城市服务");
        //1.清理所有城市服务缓存
        Set serveIconCacheKeys = redisTemplate.keys(RedisConstants.CacheName.SERVE_ICON.concat("*"));
        Set hotServeCacheKeys = redisTemplate.keys(RedisConstants.CacheName.HOT_SERVE.concat("*"));
        Set serveTypeCacheKeys = redisTemplate.keys(RedisConstants.CacheName.SERVE_TYPE.concat("*"));
        Set cacheKeys = new HashSet<>();
        cacheKeys.addAll(serveIconCacheKeys);
        cacheKeys.addAll(hotServeCacheKeys);
        cacheKeys.addAll(serveTypeCacheKeys);
        redisTemplate.delete(cacheKeys);

        //2.获取所有已启用的区域，提取区域id
        List<RegionSimpleResDTO> regionSimpleResDTOList = regionService.queryActiveRegionList();
        List<Long> activeRegionIdList = regionSimpleResDTOList.stream().map(RegionSimpleResDTO::getId).collect(Collectors.toList());

        //3.循环对每个已启用城市相关服务缓存更新
        for (Long regionId : activeRegionIdList) {
            homeService.queryServeIconCategoryByRegionIdCache(regionId);
            homeService.serveTypeList(regionId);
            homeService.hotServeList(regionId);
        }

        log.info(">>>>>>>>更新用户端首页所选城市服务完成");
    }


    /**
     * 热门服务详情缓存更新
     * 每3小时执行
     */
    /**
     * 热门服务详情缓存更新
     * 每3小时执行
     */
    @XxlJob(value = "hotServeCacheSync")
    public void hotServeCacheSync() {
        log.info(">>>>>>>>开始进行缓存同步，更新热门服务详情");

        //1.查询热门且上架状态的服务
        List<Serve> hotAndOnSaleServeList = serveService.queryHotAndOnSaleServeList();
        Set<Long> hotServeItemIds=new HashSet<>();

        //2.热门服务缓存续期
        for (Serve serve : hotAndOnSaleServeList) {
            //2.1删除热门服务缓存
            String serveKey=RedisConstants.CacheName.SERVE+"::"+serve.getId();
            redisTemplate.delete(serveKey);

            //2.2重置热门服务缓存
            homeService.hotServeList(serve.getId());

            //2.2提取热门服务对应的服务项id
            hotServeItemIds.add(serve.getServeItemId());
        }

        //3.对热门服务项更新缓存
        for (Long serveItemId : hotServeItemIds) {
            //3.1删除热门服务项缓存
            String serveKey=RedisConstants.CacheName.SERVE_ITEM+"::"+serveItemId;
            redisTemplate.delete(serveKey);

            //3.2重置热门服务项缓存
            homeService.hotServeList(serveItemId);
        }
        log.info(">>>>>>>>更新热门服务详情完成");
    }
}