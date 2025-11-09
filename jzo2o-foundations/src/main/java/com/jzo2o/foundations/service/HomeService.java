package com.jzo2o.foundations.service;

import com.jzo2o.foundations.model.dto.response.ServeAggregationSimpleResDTO;
import com.jzo2o.foundations.model.dto.response.ServeAggregationTypeSimpleResDTO;
import com.jzo2o.foundations.model.dto.response.ServeCategoryResDTO;
import com.jzo2o.foundations.model.dto.response.ServeTypeResDTO;

import java.util.List;

/**
 首页查询相关功能

 @author itcast
 @create 2023/8/21 10:55 **/
public interface HomeService {
    /**
     根据区域id获取服务图标信息

     @param regionId 区域id
     @return 服务图标列表
     */
    List<ServeCategoryResDTO> queryServeIconCategoryByRegionIdCache(Long regionId);

    List<ServeAggregationTypeSimpleResDTO> serveTypeList(Long regionId);

    List<ServeAggregationSimpleResDTO> hotServeList(Long regionId);

}