package com.jzo2o.market.mapper;

import com.jzo2o.market.model.domain.Activity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jzo2o.market.model.dto.request.ActivityQueryForPageReqDTO;
import com.jzo2o.market.model.dto.response.ActivityInfoResDTO;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author itcast
 * @since 2023-09-16
 */
public interface ActivityMapper extends BaseMapper<Activity> {

    List<ActivityInfoResDTO> queryList(ActivityQueryForPageReqDTO activityQueryForPageReqDTO);
}
