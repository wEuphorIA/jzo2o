package com.jzo2o.market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.expcetions.CommonException;
import com.jzo2o.common.expcetions.DBException;
import com.jzo2o.common.utils.DateUtils;
import com.jzo2o.common.utils.IdUtils;
import com.jzo2o.common.utils.StringUtils;
import com.jzo2o.market.enums.CouponStatusEnum;
import com.jzo2o.market.mapper.CouponIssueMapper;
import com.jzo2o.market.model.domain.Activity;
import com.jzo2o.market.model.domain.Coupon;
import com.jzo2o.market.model.domain.CouponIssue;
import com.jzo2o.market.model.dto.request.CouponIssueReqDTO;
import com.jzo2o.market.service.IActivityService;
import com.jzo2o.market.service.ICouponIssueService;
import com.jzo2o.market.service.ICouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.jzo2o.market.enums.ActivityStatusEnum.DISTRIBUTING;

/**
 @author Mr.M
 @version 1.0
 @description 发放优惠券服务类
 @date 2024/9/23 16:33 */
@Service
@Slf4j
public class CouponIssueServiceImpl extends ServiceImpl<CouponIssueMapper, CouponIssue> implements ICouponIssueService {

    //批量处理记录数
    private static final int BATCH_SIZE = 1000;

    //注入优惠券service
    @Resource
    private ICouponService couponService;

    //注入优惠券活动service
    @Resource
    private IActivityService activityService;

    @Resource
    private CouponIssueServiceImpl owner;


    @Override
    @Transactional
    public List<CouponIssue> save(CouponIssueReqDTO couponIssueReqDTO) {

        if (couponIssueReqDTO == null) {
            log.info("待发放优惠券数据为空，无需处理");
            throw new CommonException("待发放优惠券数据为空，无需处理");
        }
        //校验活动id
        if (couponIssueReqDTO.getActivityId() == null) {
            throw new CommonException("活动id不能为空");
        }
        //查询活动
        Activity activity = activityService.getById(couponIssueReqDTO.getActivityId());
        if (activity == null) {
            log.info("优惠券活动不存在");
            //抛出异常
            throw new CommonException("优惠券活动不存在");
        }
        // 活动状态为2
        if (activity.getStatus() != DISTRIBUTING.getStatus()) {
            throw new CommonException("活动状态异常");
        }
        //校验优惠券活动是否过期
        if (activity.getDistributeEndTime().isBefore(LocalDateTime.now())) {
            throw new CommonException("活动已结束");
        }
        //校验用户ids
        if (StringUtils.isBlank(couponIssueReqDTO.getUserIds())) {
            throw new CommonException("用户id不能为空");
        }

        //解析userIds
        List<Long> userIds = Arrays.stream(couponIssueReqDTO.getUserIds().split(","))
                .map(Long::parseLong).collect(Collectors.toList());
        //根据活动id和用户ids查询待发放优惠券表中存在的记录
        List<CouponIssue> couponIssueList = baseMapper.selectList(new LambdaQueryWrapper<CouponIssue>()
                .eq(CouponIssue::getActivityId, couponIssueReqDTO.getActivityId())
                .in(CouponIssue::getUserId, userIds));
        //从couponIssueList中提取出用户id
        List<Long> existUserIds = couponIssueList.stream().map(CouponIssue::getUserId).collect(Collectors.toList());
        //找到userIds不在existUserIds中的用户id
        List<Long> newUserIds = userIds.stream().filter(userId -> !existUserIds.contains(userId)).collect(Collectors.toList());
        if (newUserIds.isEmpty()) {
            return new ArrayList<>();
        }
        //newUserIds的数量
        Integer size = newUserIds.size();
        //执行sql更新activity中的库存字段，拿到扣减库存结果
        boolean b = activityService.lambdaUpdate()
                .setSql("stock_num = stock_num - " + size)
                .eq(Activity::getId, activity.getId())
                .ge(Activity::getStockNum, size)
                .update();
        if (!b) {
            throw new CommonException("优惠券活动库存不足");
        }
        List<CouponIssue> couponIssueListNew = new ArrayList<>();
        for (Long userId : newUserIds) {
            CouponIssue couponIssue = new CouponIssue();
            couponIssue.setId(IdUtils.getSnowflakeNextId());
            couponIssue.setActivityId(couponIssueReqDTO.getActivityId());
            couponIssue.setUserId(userId);
            //发放状态为0
            couponIssue.setStatus(0);
            couponIssueListNew.add(couponIssue);
        }

        //插入待发放优惠券表
        boolean b1 = saveBatch(couponIssueListNew);
        if (!b1) {
            throw new CommonException("提交待发放优惠券失败");
        }
        return couponIssueListNew;
    }


    @Override
    @Transactional
    public List<CouponIssue> issue(CouponIssueReqDTO couponIssueReqDTO) {
        //活动id
        Long activityId = couponIssueReqDTO.getActivityId();
        //查询活动信息
        Activity activity = activityService.getById(activityId);
        if (activity == null) {
            throw new CommonException("活动不存在");
        }
        //如果活动已结束不进行发放
        if (activity.getDistributeEndTime().isBefore(LocalDateTime.now())) {
            throw new CommonException("活动已结束");
        }
        //先插入到待发放优惠券表
        save(couponIssueReqDTO);
        //从couponIssueList中提取用户id
        List<Long> userIds = Arrays.asList(couponIssueReqDTO.getUserIds().split(",")).stream().map(Long::valueOf).collect(Collectors.toList());
        //根据活动id和userIds查询CouponIssue中状态为0的记录
        List<CouponIssue> couponIssues = baseMapper.selectList(
                new LambdaQueryWrapper<CouponIssue>()
                        .eq(CouponIssue::getActivityId, activityId)
                        .in(CouponIssue::getUserId, userIds)
                        .eq(CouponIssue::getStatus, 0));
        //如果couponIssues为空则不进行后续操作
        if (couponIssues.isEmpty()) {
            return couponIssues;
        }
        //更新couponIssues中的状态为1
        couponIssues.forEach(couponIssue -> couponIssue.setStatus(1));
        boolean updateBatchById = updateBatchById(couponIssues);
        if (!updateBatchById) {
            throw new DBException("优惠券发放失败");
        }
        //根据couponIssues生成List<Coupon>
        List<Coupon> couponList = couponIssues.stream().map(couponIssue -> {
                    Coupon coupon = new Coupon();
                    coupon.setId(couponIssue.getId());
                    coupon.setUserId(couponIssue.getUserId());
                    coupon.setActivityId(couponIssue.getActivityId());
                    coupon.setName(activity.getName());
                    coupon.setType(activity.getType());
                    coupon.setDiscountAmount(activity.getDiscountAmount());
                    coupon.setDiscountRate(activity.getDiscountRate());
                    coupon.setAmountCondition(activity.getAmountCondition());
                    coupon.setValidityTime(DateUtils.now().plusDays(activity.getValidityDays()));
                    coupon.setStatus(CouponStatusEnum.NO_USE.getStatus());
                    coupon.setCreateTime(DateUtils.now());
                    coupon.setUpdateTime(DateUtils.now());
                    return coupon;
                }
        ).collect(Collectors.toList());
        //将待发放优惠券数据批量插入优惠券表
        boolean b1 = couponService.saveBatch(couponList);
        if (!b1) {
            throw new CommonException("优惠券批量发放失败");
        }
        return couponIssues;
    }

    @Override
    @Transactional
    public void autoIssue(Long activityId) {
        while (true) {
            //获取待发放记录
            List<CouponIssue> couponIssueList = list(new LambdaQueryWrapper<CouponIssue>()
                    .eq(CouponIssue::getActivityId, activityId)
                    .eq(CouponIssue::getStatus, 0)
                    .last("limit " + BATCH_SIZE));
            //如果待发放记录为空，则退出
            if (CollectionUtils.isEmpty(couponIssueList)) {
                break;
            }
            log.info("待发放记录：{}", couponIssueList);
            //准备发放优惠券,创建CouponIssueReqDTO对象
            CouponIssueReqDTO couponIssueReqDTO = new CouponIssueReqDTO();
            couponIssueReqDTO.setActivityId(activityId);
            //将userIds转成字符串，中间用逗号分隔
            String userIds = StringUtils.join(",", couponIssueList.stream().map(CouponIssue::getUserId).collect(Collectors.toList()));
            couponIssueReqDTO.setUserIds(userIds);
            log.info("准备发放优惠券：{}", couponIssueReqDTO);
            try {
                owner.issue(couponIssueReqDTO);
            } catch (Exception e) {
                log.info("发放优惠券：{}异常", couponIssueReqDTO);
                e.printStackTrace();
                throw e;
            }
            //休眠1秒
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}