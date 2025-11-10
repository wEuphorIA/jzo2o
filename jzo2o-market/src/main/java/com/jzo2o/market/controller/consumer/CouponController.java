package com.jzo2o.market.controller.consumer;

import com.jzo2o.market.model.dto.response.CouponInfoResDTO;
import com.jzo2o.market.service.ICouponService;
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
    public List<CouponInfoResDTO> my(Integer status, Integer lastId){

        // 需要在表中找一个唯一的且有序的键作为排序字段，接口传入lastId，用排序字段和lastId比较，类似下边的SQL:
        // 降序：Select * from 表名  where 排序字段<lastId  limit 10
        return couponService.my(status,lastId);

    }
}
