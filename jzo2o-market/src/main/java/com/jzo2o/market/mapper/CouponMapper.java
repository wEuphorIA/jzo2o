package com.jzo2o.market.mapper;

import com.jzo2o.market.model.domain.Coupon;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jzo2o.market.model.dto.request.CouponOperationPageQueryReqDTO;
import com.jzo2o.market.model.dto.response.CouponInfoResDTO;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author itcast
 * @since 2023-09-16
 */
public interface CouponMapper extends BaseMapper<Coupon> {

    List<CouponInfoResDTO> queryList(CouponOperationPageQueryReqDTO couponOperationPageQueryReqDTO);
}
