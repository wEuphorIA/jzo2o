package com.jzo2o.market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.api.market.dto.response.AvailableCouponsResDTO;
import com.jzo2o.common.expcetions.BadRequestException;
import com.jzo2o.common.model.CurrentUser;
import com.jzo2o.common.model.CurrentUserInfo;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.common.utils.*;
import com.jzo2o.market.enums.CouponStatusEnum;
import com.jzo2o.market.mapper.CouponMapper;
import com.jzo2o.market.model.domain.Coupon;
import com.jzo2o.market.model.dto.request.CouponOperationPageQueryReqDTO;
import com.jzo2o.market.model.dto.response.CouponInfoResDTO;
import com.jzo2o.market.service.IActivityService;
import com.jzo2o.market.service.ICouponService;
import com.jzo2o.market.service.ICouponUseBackService;
import com.jzo2o.market.service.ICouponWriteOffService;
import com.jzo2o.market.utils.CouponUtils;
import com.jzo2o.mysql.utils.PageHelperUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public List<CouponInfoResDTO> my(Integer status, Long lastId) {

        Long userId = UserContext.currentUserId();
        // 1.校验
        if (status > 3 || status < 1) {
            throw new BadRequestException("请求状态不存在");
        }
        // 2.查询准备
        LambdaQueryWrapper<Coupon> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 查询条件
        lambdaQueryWrapper.eq(Coupon::getStatus, status)
                .eq(Coupon::getUserId, userId)
                .lt(ObjectUtils.isNotNull(lastId), Coupon::getId, lastId);
        // 查询字段
        lambdaQueryWrapper.select(Coupon::getId);
        // 排序
        lambdaQueryWrapper.orderByDesc(Coupon::getId);
        // 查询条数限制
        lambdaQueryWrapper.last(" limit 10 ");
        // 3.查询数据(数据中只含id)
        List<Coupon> couponsOnlyId = baseMapper.selectList(lambdaQueryWrapper);
        //判空
        if (CollUtils.isEmpty(couponsOnlyId)) {
            return new ArrayList<>();
        }

        // 4.获取数据且数据转换
        // 优惠id列表
        List<Long> ids = couponsOnlyId.stream()
                .map(Coupon::getId)
                .collect(Collectors.toList());
        // 获取优惠券数据
        List<Coupon> coupons = baseMapper.selectBatchIds(ids);
        // 数据转换
        return BeanUtils.copyToList(coupons, CouponInfoResDTO.class);
    }

    @Override
    public List<AvailableCouponsResDTO> getAvailable(Long userId, BigDecimal totalAmount) {
        // 1.查询优惠券
        List<Coupon> coupons = lambdaQuery()
                .eq(Coupon::getStatus, CouponStatusEnum.NO_USE.getStatus())
                .gt(Coupon::getValidityTime, DateUtils.now())
                .le(Coupon::getAmountCondition, totalAmount)
                .eq(Coupon::getUserId, userId)
                .list();
        // 判空
        if (CollUtils.isEmpty(coupons)) {
            return new ArrayList<>();
        }
        // 2.组装数据计数优惠金额
        return coupons.stream()
                //先计算优惠金额
                .peek(coupon -> coupon.setDiscountAmount(CouponUtils.calDiscountAmount(coupon, totalAmount)))
                //过滤优惠金额小于订单金额的优惠券
                .filter(coupon -> coupon.getDiscountAmount().compareTo(totalAmount)<0)
                // 计算金额
                .map(coupon -> BeanUtils.copyBean(coupon, AvailableCouponsResDTO.class))
                //按优惠金额降序排
                .sorted(Comparator.comparing(AvailableCouponsResDTO::getDiscountAmount).reversed())
                .collect(Collectors.toList());
    }


}
