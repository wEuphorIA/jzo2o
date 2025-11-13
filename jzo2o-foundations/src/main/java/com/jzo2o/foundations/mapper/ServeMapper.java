package com.jzo2o.foundations.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jzo2o.api.foundations.dto.response.ServeAggregationResDTO;
import com.jzo2o.foundations.model.domain.Serve;
import com.jzo2o.foundations.model.dto.response.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author itcast
 * @since 2023-07-03
 */
public interface ServeMapper extends BaseMapper<Serve> {
    /**
     * 根据区域查询服务列表
     * @param regionId
     * @return
     */
    List<ServeResDTO> queryServeListByRegionId(@Param("regionId") Long regionId);

    List<ServeCategoryResDTO> findServeIconCategoryByRegionId(@Param("regionId")Long regionId);

    List<ServeAggregationTypeSimpleResDTO> queryServeTypeListByRegionId(Long regionId);

    List<ServeAggregationSimpleResDTO> queryHotServeListByRegionId(Long regionId);

    ServeAggregationResDTO findServeDetailById(Long id);

    ServeAggregationSimpleResDTO findById(Long id);
}
