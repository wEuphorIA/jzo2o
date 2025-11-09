package com.jzo2o.foundations.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.google.common.util.concurrent.ServiceManager;
import com.jzo2o.foundations.constants.RedisConstants;
import com.jzo2o.foundations.enums.FoundationStatusEnum;
import com.jzo2o.foundations.mapper.RegionMapper;
import com.jzo2o.foundations.mapper.ServeMapper;
import com.jzo2o.foundations.model.domain.Region;
import com.jzo2o.foundations.model.dto.response.*;
import com.jzo2o.foundations.service.HomeService;
import com.jzo2o.foundations.service.IRegionService;
import com.jzo2o.foundations.service.IServeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class HomeServiceImpl implements HomeService {

    @Resource
    private IRegionService regionService;

    @Resource
    private ServeMapper serveMapper;


    /**
     根据区域id查询已开通的服务类型

     @param regionId 区域id
     @return 已开通的服务类型
     */
    @Override
    @Caching(
            cacheable = {
                    //result为null时,属于缓存穿透情况，缓存时间30分钟
                    @Cacheable(value = RedisConstants.CacheName.SERVE_ICON, key = "#regionId", unless = "#result.size() != 0", cacheManager = RedisConstants.CacheManager.THIRTY_MINUTES),
                    //result不为null时,永久缓存
                    @Cacheable(value = RedisConstants.CacheName.SERVE_ICON, key = "#regionId", unless = "#result.size() == 0", cacheManager = RedisConstants.CacheManager.FOREVER)
            }
    )
    public List<ServeCategoryResDTO> queryServeIconCategoryByRegionIdCache(Long regionId) {
        //1.校验当前城市是否为启用状态
        Region region = regionService.getById(regionId);
        if (ObjectUtil.isEmpty(region) || ObjectUtil.equal(FoundationStatusEnum.DISABLE.getStatus(), region.getActiveStatus())) {
            return Collections.emptyList();
        }

        //2.根据城市编码查询所有的服务图标
        List<ServeCategoryResDTO> list = serveMapper.findServeIconCategoryByRegionId(regionId);
        if (ObjectUtil.isEmpty(list)) {
            return Collections.emptyList();
        }

        //3.服务类型取前两个，每个类型下服务项取前4个
        //list的截止下标
        int endIndex = list.size() >= 2 ? 2 : list.size();
        List<ServeCategoryResDTO> serveCategoryResDTOS = new ArrayList<>(list.subList(0, endIndex));
        serveCategoryResDTOS.forEach(v -> {
            List<ServeSimpleResDTO> serveResDTOList = v.getServeResDTOList();
            //serveResDTOList的截止下标
            int endIndex2 = serveResDTOList.size() >= 4 ? 4 : serveResDTOList.size();
            List<ServeSimpleResDTO> serveSimpleResDTOS = new ArrayList<>(serveResDTOList.subList(0, endIndex2));
            v.setServeResDTOList(serveSimpleResDTOS);
        });

        return serveCategoryResDTOS;
    }


    @Override
    @Caching(
            cacheable = {
                    //result为null时,属于缓存穿透情况，缓存时间30分钟
                    @Cacheable(value = RedisConstants.CacheName.SERVE_TYPE, key = "#regionId", unless = "#result.size() != 0", cacheManager = RedisConstants.CacheManager.THIRTY_MINUTES),
                    //result不为null时,永久缓存
                    @Cacheable(value = RedisConstants.CacheName.SERVE_TYPE, key = "#regionId", unless = "#result.size() == 0", cacheManager = RedisConstants.CacheManager.FOREVER)
            }
    )
    public List<ServeAggregationTypeSimpleResDTO> serveTypeList(Long regionId) {
        return serveMapper.queryServeTypeListByRegionId(regionId);
    }

    @Override
    @Caching(
            cacheable = {
                    //result为null时,属于缓存穿透情况，缓存时间30分钟
                    @Cacheable(value = RedisConstants.CacheName.HOT_SERVE, key = "#regionId", unless = "#result.size() != 0", cacheManager = RedisConstants.CacheManager.THIRTY_MINUTES),
                    //result不为null时,永久缓存
                    @Cacheable(value = RedisConstants.CacheName.HOT_SERVE, key = "#regionId", unless = "#result.size() == 0", cacheManager = RedisConstants.CacheManager.FOREVER)
            }
    )
    public List<ServeAggregationSimpleResDTO> hotServeList(Long regionId) {
        return serveMapper.queryHotServeListByRegionId(regionId);
    }

}