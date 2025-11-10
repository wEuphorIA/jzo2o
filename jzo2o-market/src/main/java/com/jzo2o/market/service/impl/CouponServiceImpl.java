package com.jzo2o.market.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.market.enums.CouponStatusEnum;
import com.jzo2o.market.mapper.CouponMapper;
import com.jzo2o.market.model.domain.Coupon;
import com.jzo2o.market.model.dto.request.CouponOperationPageQueryReqDTO;
import com.jzo2o.market.model.dto.response.CouponInfoResDTO;
import com.jzo2o.market.service.IActivityService;
import com.jzo2o.market.service.ICouponService;
import com.jzo2o.market.service.ICouponUseBackService;
import com.jzo2o.market.service.ICouponWriteOffService;
import com.jzo2o.mysql.utils.PageHelperUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author itcast
 * @since 2023-09-16
 */
@Service
@Slf4j
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements ICouponService {

    @Resource(name = "seizeCouponScript")
    private DefaultRedisScript<String> seizeCouponScript;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private IActivityService activityService;

    @Resource
    private ICouponUseBackService couponUseBackService;

    @Resource
    private ICouponWriteOffService couponWriteOffService;




    @Override
    public PageResult<CouponInfoResDTO> page(CouponOperationPageQueryReqDTO couponOperationPageQueryReqDTO) {
        return PageHelperUtils.selectPage(couponOperationPageQueryReqDTO,
                () -> baseMapper.queryList(couponOperationPageQueryReqDTO));
    }

    @Override
    @Transactional
    public void processExpireCoupon() {
        List<Coupon> coupons = baseMapper.selectList(Wrappers.<Coupon>lambdaQuery()
                .eq(Coupon::getStatus, CouponStatusEnum.NO_USE.getStatus())
                .lt(Coupon::getValidityTime, LocalDateTime.now()));
        for (Coupon coupon : coupons) {
            coupon.setStatus(CouponStatusEnum.INVALID.getStatus());
        }
        updateBatchById(coupons);
    }
}
