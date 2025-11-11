package com.jzo2o.market.controller.consumer;

import com.jzo2o.market.model.dto.response.CouponInfoResDTO;
import com.jzo2o.market.service.ICouponService;
import com.jzo2o.market.service.ICouponUseBackService;
import com.jzo2o.market.service.ICouponWriteOffService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 @author Euphoria
 @version 1.0
 @description: TODO
 @date 2025/11/10 上午9:31 */
@RestController("consumerCouponController")
@RequestMapping("/consumer/coupon")
@Api(tags = "用户优惠券")
public class CouponController {

    @Resource
    private ICouponService couponService;



    @GetMapping("my")
    public List<CouponInfoResDTO> my(Integer status, Long lastId){

        return couponService.my(status,lastId);

    }
}
