package com.jzo2o.market.handler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jzo2o.market.model.domain.Activity;
import com.jzo2o.market.service.IActivityService;
import com.jzo2o.market.service.ICouponIssueService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.jzo2o.market.enums.ActivityStatusEnum.DISTRIBUTING;

/**
 @author Euphoria
 @version 1.0
 @description: TODO
 @date 2025/11/13 下午3:08 */
@Slf4j
@Component
public class IssuedCouponHandlerJob {

    //定义线程池
    private static final ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private ICouponIssueService couponIssueService;

    @Resource
    private IActivityService activityService;

    @Resource
    private RedissonClient redissonClient;

    static {
        threadPoolExecutor = new ThreadPoolExecutor(0, 20, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100));
    }

    public void start() {
        log.info("自动发放优惠券任务开始");
        //从活动表查询还未结束的活动。
        List<Activity> activityList = activityService.list(new LambdaQueryWrapper<Activity>()
                .in(Activity::getStatus, DISTRIBUTING.getStatus()));
        //以活动id为单位创建优惠券发放任务，并加入线程池
        activityList.forEach(activity -> {
            //创建IssuedCouponHandler对象
            IssuedCouponHandler issuedCouponHandler = new IssuedCouponHandler(activity.getId(), couponIssueService,redissonClient);
            //将任务加入线程池
            threadPoolExecutor.execute(issuedCouponHandler);
        });

    }
}
