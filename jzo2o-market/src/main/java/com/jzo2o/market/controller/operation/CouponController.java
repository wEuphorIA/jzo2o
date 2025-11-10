package com.jzo2o.market.controller.operation;

import com.jzo2o.common.model.PageResult;
import com.jzo2o.market.model.dto.request.CouponOperationPageQueryReqDTO;
import com.jzo2o.market.model.dto.response.CouponInfoResDTO;
import com.jzo2o.market.service.ICouponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 @author Euphoria
 @version 1.0
 @description: TODO
 @date 2025/11/9 下午4:45 */
@RestController
@RequestMapping("/operation/coupon")
@Api(tags = "优惠券管理")
public class CouponController {

    @Resource
    private ICouponService couponService;

    @GetMapping("page")
    @ApiOperation("优惠券列表")
    public PageResult<CouponInfoResDTO> page(CouponOperationPageQueryReqDTO couponOperationPageQueryReqDTO){
        return couponService.page(couponOperationPageQueryReqDTO);
    }
}
