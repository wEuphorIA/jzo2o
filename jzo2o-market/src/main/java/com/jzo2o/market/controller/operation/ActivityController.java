package com.jzo2o.market.controller.operation;

import cn.hutool.core.bean.BeanUtil;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.market.model.domain.Activity;
import com.jzo2o.market.model.dto.request.ActivityQueryForPageReqDTO;
import com.jzo2o.market.model.dto.request.ActivitySaveReqDTO;
import com.jzo2o.market.model.dto.response.ActivityInfoResDTO;
import com.jzo2o.market.service.IActivityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

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

    @GetMapping("/page")
    @ApiOperation("分页查询优惠券活动")
    public PageResult<ActivityInfoResDTO> page(ActivityQueryForPageReqDTO activityQueryForPageReqDTO){
        return activityService.page(activityQueryForPageReqDTO);
    }

    @PostMapping("/save")
    @ApiOperation("保存优惠券活动")
    public void save(@RequestBody ActivitySaveReqDTO activitySaveReqDTO){
        //本项目优惠券只支持满减与折扣两种类型优惠券类型，1：满减，2：折扣
        activitySaveReqDTO.check();
        Activity activity = BeanUtil.toBean(activitySaveReqDTO, Activity.class);
        activity.setStatus(1);
        if (activitySaveReqDTO.getId() == null){
            activity.setCreateBy(UserContext.currentUserId());
        }
        activity.setUpdateBy(UserContext.currentUserId());
        activityService.saveOrUpdate(activity);
    }

    @GetMapping("/{id}")
    @ApiOperation("查询优惠券活动")
    public ActivityInfoResDTO get(@PathVariable Long id){
        return BeanUtil.toBean(activityService.getById(id), ActivityInfoResDTO.class);
    }


}
