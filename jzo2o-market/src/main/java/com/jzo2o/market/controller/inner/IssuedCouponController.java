package com.jzo2o.market.controller.inner;

import com.jzo2o.market.model.domain.CouponIssue;
import com.jzo2o.market.model.dto.request.CouponIssueReqDTO;
import com.jzo2o.market.service.ICouponIssueService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController("innerIssuedCouponController")
@RequestMapping("/inner/issuedcoupon")
@Api(tags = "内部接口-发放优惠券相关接口")
public class IssuedCouponController {

    @Resource
    private ICouponIssueService couponIssueService;

    @ApiOperation("提交待发放优惠券数据")
    @PostMapping("/save")
    public List<CouponIssue> save(@RequestBody CouponIssueReqDTO couponIssueReqDTO) {
        return couponIssueService.save(couponIssueReqDTO);
    }


    @ApiOperation("立即发放优惠券")
    @PostMapping("/issue")
    public List<CouponIssue> issue(@RequestBody  CouponIssueReqDTO couponIssueReqDTO) {
        return couponIssueService.issue(couponIssueReqDTO);
    }
}