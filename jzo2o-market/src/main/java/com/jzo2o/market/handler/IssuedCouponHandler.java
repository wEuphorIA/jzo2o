package com.jzo2o.market.handler;

import com.jzo2o.market.service.ICouponIssueService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

@Slf4j
public class IssuedCouponHandler implements Runnable {


    //优惠券发放service
    private final ICouponIssueService couponIssueService;

    //分布式锁
    private final RedissonClient redissonClient;

    //活动id
    private final Long activityId;

    //构造方法
    public IssuedCouponHandler(Long activityId,ICouponIssueService couponIssueService,RedissonClient redissonClient) {
        this.activityId = activityId;
        this.couponIssueService = couponIssueService;
        this.redissonClient = redissonClient;
    }

    public void run() {
        //获取锁
        String lockKey = "activity:issued:lock:" + activityId;
        log.info("获取锁：{}", lockKey);
        RLock lock = redissonClient.getLock(lockKey);
        //尝试获取锁
        try {
            boolean tryLock = lock.tryLock(1,-1, TimeUnit.SECONDS);
            if(!tryLock){
                log.info("获取锁失败：{}", lockKey);
                return;
            }
            try {
                //开始发放优惠券
                log.info("开始发放优惠券：{}", activityId);
                //批量发放优惠券
                couponIssueService.autoIssue(activityId);
            } catch (Exception e) {
                log.error("发放优惠券失败：{}", e.getMessage());
            }finally {
                lock.unlock();
            }
    
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }
}