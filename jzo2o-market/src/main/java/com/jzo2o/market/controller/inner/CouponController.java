package com.jzo2o.market.controller.inner;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jzo2o.api.market.CouponApi;
import com.jzo2o.api.market.dto.request.CouponUseBackReqDTO;
import com.jzo2o.api.market.dto.request.CouponUseReqDTO;
import com.jzo2o.api.market.dto.response.AvailableCouponsResDTO;
import com.jzo2o.api.market.dto.response.CouponUseResDTO;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.market.enums.CouponStatusEnum;
import com.jzo2o.market.model.domain.Coupon;
import com.jzo2o.market.model.domain.CouponUseBack;
import com.jzo2o.market.model.domain.CouponWriteOff;
import com.jzo2o.market.service.ICouponService;
//import io.seata.spring.annotation.GlobalTransactional;
import com.jzo2o.market.service.ICouponUseBackService;
import com.jzo2o.market.service.ICouponWriteOffService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.jzo2o.common.utils.UserContext.getCurrentUser;
import static com.jzo2o.market.enums.ActivityTypeEnum.AMOUNT_DISCOUNT;
import static com.jzo2o.market.enums.ActivityTypeEnum.RATE_DISCOUNT;

@RestController("innerCouponController")
@RequestMapping("/inner/coupon")
@Api(tags = "内部接口-优惠券相关接口")
public class CouponController implements CouponApi {

    @Resource
    private ICouponService couponService;

    @Resource
    private ICouponWriteOffService couponWriteOffService;

    @Resource
    private ICouponUseBackService couponUseBackService;

    @Override
    @GetMapping("/getAvailable")
    @ApiOperation("获取可用优惠券列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "totalAmount", value = "总金额，单位分", required = true, dataTypeClass = BigDecimal.class),
            @ApiImplicitParam(name = "userId", value = "用户id", required = true, dataTypeClass = BigDecimal.class)
    })
    public List<AvailableCouponsResDTO> getAvailable(@RequestParam("userId") Long userId, @RequestParam("totalAmount") BigDecimal totalAmount) {
        return couponService.getAvailable(userId, totalAmount);
    }

    @Override
    @PostMapping("/use")
    @ApiOperation("使用优惠券，并返回优惠金额")
    @Transactional
    public CouponUseResDTO use(CouponUseReqDTO couponUseReqDTO) {
        //当用户成功使用一张优惠券会在优惠券核销表记录一条记录，记录是哪个用户的哪个订单使用了哪个优惠券。关键字段：用户id、优惠券id、订单id，核销时间。
        Coupon coupon = couponService.getById(couponUseReqDTO.getId());
        if (coupon == null) {
            throw new RuntimeException("优惠券不存在");
        }

        //- 限制：订单金额大于等于满减金额。
        // - 限制：优惠券有效
        if (coupon.getAmountCondition().compareTo(couponUseReqDTO.getTotalAmount()) > 0) {
            throw new RuntimeException("订单金额小于优惠券金额");
        }

        if (coupon.getStatus() != CouponStatusEnum.NO_USE.getStatus()) {
            throw new RuntimeException("优惠券不可用");
        }

        //有效期得在当前时间之后
        if (coupon.getValidityTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("优惠券已失效");
        }

        // - 根据优惠券id标记优惠券表中该优惠券已使用、使用时间、订单id等。
        coupon.setStatus(CouponStatusEnum.USED.getStatus());
        coupon.setUseTime(LocalDateTime.now());
        coupon.setOrdersId(String.valueOf(couponUseReqDTO.getOrdersId()));
        coupon.setUpdateTime(LocalDateTime.now());
        couponService.updateById(coupon);

        CouponWriteOff couponWriteOff = CouponWriteOff.builder()
                .couponId(couponUseReqDTO.getId())
                .userId(coupon.getUserId())
                .ordersId(couponUseReqDTO.getOrdersId())
                .activityId(coupon.getActivityId())
                .writeOffTime(coupon.getUseTime())
                .writeOffManPhone(coupon.getUserPhone())
                .writeOffManName(coupon.getUserName())
                .build();
        couponWriteOffService.save(couponWriteOff);

        CouponUseResDTO couponUseResDTO = new CouponUseResDTO();
        if (coupon.getType() == AMOUNT_DISCOUNT.getType()) {
            couponUseResDTO.setDiscountAmount(coupon.getDiscountAmount());
        } else if (coupon.getType() == RATE_DISCOUNT.getType()) {
            couponUseResDTO.setDiscountAmount(couponUseReqDTO.getTotalAmount().multiply(BigDecimal.valueOf(1 - (coupon.getDiscountRate() / 100.0))));
        }
        return couponUseResDTO;
    }

    @Override
    @PostMapping("/useBack")
    @ApiOperation("优惠券退回接口")
    @Transactional
    public void useBack(CouponUseBackReqDTO couponUseBackReqDTO) {
        CouponWriteOff couponWriteOff = couponWriteOffService.getOne(Wrappers.<CouponWriteOff>lambdaQuery()
                .eq(CouponWriteOff::getCouponId, couponUseBackReqDTO.getId()));

        if (couponWriteOff == null) {
            throw new RuntimeException("优惠券核销状态异常");
        }

        Coupon coupon = couponService.getById(couponWriteOff.getCouponId());
        if (coupon == null) {
            throw new RuntimeException("优惠券不存在");
        }

        //如果想回退的话必须得状态为已使用
        if (coupon.getStatus() != CouponStatusEnum.USED.getStatus()) {
            throw new RuntimeException("优惠券不可用");
        }


        CouponUseBack couponUseBack = new CouponUseBack();
        couponUseBack.setCouponId(couponUseBackReqDTO.getId());
        couponUseBack.setUserId(couponUseBackReqDTO.getUserId());
        couponUseBack.setUseBackTime(LocalDateTime.now());
        couponUseBack.setWriteOffTime(couponWriteOff.getWriteOffTime());

        couponUseBackService.save(couponUseBack);

        LambdaUpdateWrapper<Coupon> wrapper = new LambdaUpdateWrapper<>();
        if (coupon.getValidityTime().isBefore(LocalDateTime.now())) {
            wrapper.set(Coupon::getStatus, CouponStatusEnum.VOIDED.getStatus());
        } else {
            wrapper.set(Coupon::getStatus, CouponStatusEnum.NO_USE.getStatus());
        }
        wrapper.set(Coupon::getUseTime, null);
        wrapper.set(Coupon::getOrdersId, null);
        wrapper.set(Coupon::getUpdateTime, LocalDateTime.now());
        wrapper.eq(Coupon::getId, coupon.getId());
        couponService.update(wrapper);
        couponWriteOffService.removeById(couponWriteOff);
    }
}