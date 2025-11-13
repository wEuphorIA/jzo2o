package com.jzo2o.market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.market.model.domain.Coupon;
import com.jzo2o.market.model.domain.CouponIssue;
import com.jzo2o.market.model.dto.request.CouponIssueReqDTO;

import java.util.List;

public interface ICouponIssueService extends IService<CouponIssue> {

    List<CouponIssue> save(CouponIssueReqDTO couponIssueReqDTO);

    List<CouponIssue> issue(CouponIssueReqDTO couponIssueReqDTO);

    void autoIssue(Long activityId);
}
