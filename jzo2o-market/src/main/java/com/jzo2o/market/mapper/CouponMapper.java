package com.jzo2o.market.mapper;

import com.jzo2o.api.market.dto.response.AvailableCouponsResDTO;
import com.jzo2o.market.model.domain.Coupon;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jzo2o.market.model.dto.request.CouponOperationPageQueryReqDTO;
import com.jzo2o.market.model.dto.response.CouponInfoResDTO;

import java.math.BigDecimal;
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

    List<CouponInfoResDTO> my(Integer status, Integer lastId, Long aLong);

    List<AvailableCouponsResDTO> getAvailable(Long userId, BigDecimal totalAmount);
}
