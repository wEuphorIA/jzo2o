package com.jzo2o.market.controller.operation;

import cn.hutool.core.bean.BeanUtil;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.common.utils.ObjectUtils;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.market.model.domain.Activity;
import com.jzo2o.market.model.domain.Coupon;
import com.jzo2o.market.model.dto.request.ActivityQueryForPageReqDTO;
import com.jzo2o.market.model.dto.request.ActivitySaveReqDTO;
import com.jzo2o.market.model.dto.response.ActivityInfoResDTO;
import com.jzo2o.market.service.IActivityService;
import com.jzo2o.market.service.ICouponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static com.jzo2o.market.enums.ActivityStatusEnum.NO_DISTRIBUTE;
import static com.jzo2o.market.enums.CouponStatusEnum.*;

/**
 @author Euphoria
 @version 1.0
 @description: TODO
 @date 2025/11/9 下午4:04 */
@RestController
@RequestMapping("/operation/activity")
@Api(tags = "优惠券活动管理")
public class ActivityController {

    @Resource
    private IActivityService activityService;

    @Resource
    private ICouponService couponService;

    @GetMapping("/page")
    @ApiOperation("分页查询优惠券活动")
    public PageResult<ActivityInfoResDTO> page(ActivityQueryForPageReqDTO activityQueryForPageReqDTO) {
        return activityService.page(activityQueryForPageReqDTO);
    }

    @PostMapping("/save")
    @ApiOperation("保存优惠券活动")
    public void save(@RequestBody ActivitySaveReqDTO activitySaveReqDTO) {
        //本项目优惠券只支持满减与折扣两种类型优惠券类型，1：满减，2：折扣
        activitySaveReqDTO.check();
        Activity activity = BeanUtil.toBean(activitySaveReqDTO, Activity.class);
        activity.setStatus(NO_DISTRIBUTE.getStatus());
        if (activitySaveReqDTO.getId() == null) {
            activity.setCreateBy(UserContext.currentUserId());
        }
        activity.setUpdateBy(UserContext.currentUserId());
        activity.setStockNum(activity.getTotalNum());
        activityService.saveOrUpdate(activity);
    }

    @GetMapping("/{id}")
    @ApiOperation("查询优惠券活动")
    public ActivityInfoResDTO get(@PathVariable Long id) {
        ActivityInfoResDTO activityInfoResDTO = BeanUtil.toBean(activityService.getById(id), ActivityInfoResDTO.class);
        //领取数量
        activityInfoResDTO.setReceiveNum(couponService.lambdaQuery().eq(Coupon::getActivityId, id).count().intValue());
        //核销的数量
        activityInfoResDTO.setWriteOffNum(couponService.lambdaQuery().eq(Coupon::getActivityId, id).eq(Coupon::getStatus, USED.getStatus()).count().intValue());
        return activityInfoResDTO;
    }

    @PostMapping("/revoke/{id}")
    @ApiOperation("撤销优惠券活动")
    @Transactional
    public void revoke(@PathVariable Long id) {
        Activity activity = activityService.getById(id);
        if (ObjectUtils.isNull(activity)) {
            throw new RuntimeException("活动不存在");
        }
        //只允许对待生效及进行中的活动进行撤销。
        if (!(activity.getStatus() == NO_USE.getStatus() || activity.getStatus() == USED.getStatus())) {
            throw new RuntimeException("只允许对待生效及进行中的活动进行撤销。");
        }

        activity.setStatus(VOIDED.getStatus());
        activityService.updateById(activity);

        //还需要将所有抢到本活动优惠券的状态为未使用的记录的状态更改为“已失效” 。
        couponService.lambdaUpdate().eq(Coupon::getActivityId, id).eq(Coupon::getStatus, USED.getStatus()).set(Coupon::getStatus, VOIDED.getStatus()).update();
    }
}
