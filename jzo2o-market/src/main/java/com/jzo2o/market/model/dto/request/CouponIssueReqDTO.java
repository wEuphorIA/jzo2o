package com.jzo2o.market.model.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 发放优惠券
 * </p>
 *
 * @author mrt
 * @since 2024-09-23
 */
@Data
public class CouponIssueReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 优惠券活动id
     */
    @ApiModelProperty(value = "优惠券活动id",required = true)
    private Long activityId;

    /**
     * 用户id，多个用户用逗号隔开
     */
    @ApiModelProperty(value = "用户id，多个用户用逗号隔开",required = true)
    private String userIds;


}