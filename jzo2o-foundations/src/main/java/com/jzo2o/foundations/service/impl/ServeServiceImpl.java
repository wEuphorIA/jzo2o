package com.jzo2o.foundations.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.api.foundations.dto.response.ServeAggregationResDTO;
import com.jzo2o.common.expcetions.CommonException;
import com.jzo2o.common.expcetions.ForbiddenOperationException;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.common.utils.BeanUtils;
import com.jzo2o.foundations.constants.RedisConstants;
import com.jzo2o.foundations.enums.FoundationStatusEnum;
import com.jzo2o.foundations.mapper.RegionMapper;
import com.jzo2o.foundations.mapper.ServeItemMapper;
import com.jzo2o.foundations.mapper.ServeMapper;
import com.jzo2o.foundations.model.domain.Region;
import com.jzo2o.foundations.model.domain.Serve;
import com.jzo2o.foundations.model.domain.ServeItem;
import com.jzo2o.foundations.model.dto.request.ServePageQueryReqDTO;
import com.jzo2o.foundations.model.dto.request.ServeUpsertReqDTO;
import com.jzo2o.foundations.model.dto.response.ServeAggregationSimpleResDTO;
import com.jzo2o.foundations.model.dto.response.ServeResDTO;
import com.jzo2o.foundations.service.IServeService;
import com.jzo2o.mysql.utils.PageHelperUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 <p>
 服务实现类
 </p>

 @author itcast
 @since 2023-07-03 */
@Service
public class ServeServiceImpl extends ServiceImpl<ServeMapper, Serve> implements IServeService {

    @Resource
    private ServeItemMapper serveItemMapper;

    @Resource
    private RegionMapper regionMapper;

    /**
     分页查询

     @param servePageQueryReqDTO 查询条件
     @return 分页结果
     */
    @Override
    public PageResult<ServeResDTO> page(ServePageQueryReqDTO servePageQueryReqDTO) {
        //通过baseMapper调用queryServeListByRegionId方法
        return PageHelperUtils.selectPage(servePageQueryReqDTO, () -> baseMapper.queryServeListByRegionId(servePageQueryReqDTO.getRegionId()));
    }

    @Override
    @Transactional
    public void batchAdd(List<ServeUpsertReqDTO> serveUpsertReqDTOList) {
        List<Serve> serves = new ArrayList<>();
        for (ServeUpsertReqDTO serveUpsertReqDTO : serveUpsertReqDTOList) {
            //1.校验服务项是否为启用状态，不是启用状态不能新增
            ServeItem serveItem = serveItemMapper.selectById(serveUpsertReqDTO.getServeItemId());
            if (serveItem == null || serveItem.getActiveStatus() != FoundationStatusEnum.ENABLE.getStatus()) {
                throw new ForbiddenOperationException("服务项不存在或未启用");
            }

            //2.校验区域是否为启用状态，不是启用状态不能新增
            Region region = regionMapper.selectById(serveUpsertReqDTO.getRegionId());
            if (region == null || region.getActiveStatus() != FoundationStatusEnum.ENABLE.getStatus()) {
                throw new ForbiddenOperationException("区域不存在或未启用");
            }

            //3.校验是否存在不能重复添加
            Long count = lambdaQuery().eq(Serve::getServeItemId, serveUpsertReqDTO.getServeItemId())
                    .eq(Serve::getRegionId, serveUpsertReqDTO.getRegionId()).count();

            if (count > 0) {
                throw new ForbiddenOperationException(serveItem.getName() + "服务已存在");
            }

            Serve serve = BeanUtils.copyBean(serveUpsertReqDTO, Serve.class);
            serve.setPrice(serveItem.getReferencePrice());
            serve.setCityCode(region.getCityCode());

            serves.add(serve);
        }
        saveBatch(serves);
    }

    @Override
    public void update(Long id, BigDecimal price) {
        boolean update = lambdaUpdate().set(Serve::getPrice, price).eq(Serve::getId, id).update();
        if (!update) {
            throw new CommonException("修改服务价格失败");
        }
    }

    @Override
    @CachePut(value = RedisConstants.CacheName.SERVE, key = "#id",  cacheManager = RedisConstants.CacheManager.ONE_DAY)
    public Serve onSale(Long id) {
        Serve serve = baseMapper.selectById(id);

        if (serve == null) {
            throw new CommonException("服务不存在");
        }

        //服务的状态不能为已上架
        if (serve.getSaleStatus() == FoundationStatusEnum.ENABLE.getStatus()) {
            throw new ForbiddenOperationException("草稿或下架状态方可上架");
        }

        //服务项id
        Long serveItemId = serve.getServeItemId();
        ServeItem serveItem = serveItemMapper.selectById(serveItemId);
        if (ObjectUtil.isNull(serveItem)) {
            throw new ForbiddenOperationException("所属服务项不存在");
        }
        //服务项的启用状态
        Integer activeStatus = serveItem.getActiveStatus();

        //服务项为启用状态方可上架
        if (!(FoundationStatusEnum.ENABLE.getStatus() == activeStatus)) {
            throw new ForbiddenOperationException("服务项为启用状态方可上架");
        }

        //区域id
        Long regionId = serve.getRegionId();
        Region region = regionMapper.selectById(regionId);
        if (ObjectUtil.isNull(region)) {
            throw new ForbiddenOperationException("所属区域不存在");
        }

        boolean update = lambdaUpdate()
                .set(Serve::getSaleStatus, FoundationStatusEnum.ENABLE.getStatus())
                .eq(Serve::getId, id).update();

        if (!update) {
            throw new CommonException("服务上架失败");
        }

        return serve;
    }

    @Override
    public void delete(Long id) {

        Serve serve = baseMapper.selectById(id);

        if (serve == null) {
            throw new CommonException("服务不存在");
        }

        //服务的状态只能为草稿
        if (serve.getSaleStatus() != FoundationStatusEnum.INIT.getStatus()) {
            throw new ForbiddenOperationException("只有草稿状态方可删除");
        }

        //服务项id
        Long serveItemId = serve.getServeItemId();
        ServeItem serveItem = serveItemMapper.selectById(serveItemId);
        if (ObjectUtil.isNull(serveItem)) {
            throw new ForbiddenOperationException("所属服务项不存在");
        }

        //区域id
        Long regionId = serve.getRegionId();
        Region region = regionMapper.selectById(regionId);
        if (ObjectUtil.isNull(region)) {
            throw new ForbiddenOperationException("所属区域不存在");
        }

        boolean delete = removeById(id);

        if (!delete) {
            throw new CommonException("服务删除失败");
        }
    }

    @Override
    @CacheEvict(value = RedisConstants.CacheName.SERVE, key = "#id")
    public Serve offSale(Long id) {

        Serve serve = baseMapper.selectById(id);

        if (serve == null) {
            throw new CommonException("服务不存在");
        }

        //服务的状态必须为已上架
        if (serve.getSaleStatus() != FoundationStatusEnum.ENABLE.getStatus()) {
            throw new ForbiddenOperationException("只有已上架状态方可下架");
        }

        //服务项id
        Long serveItemId = serve.getServeItemId();
        ServeItem serveItem = serveItemMapper.selectById(serveItemId);
        if (ObjectUtil.isNull(serveItem)) {
            throw new ForbiddenOperationException("所属服务项不存在");
        }

        //区域id
        Long regionId = serve.getRegionId();
        Region region = regionMapper.selectById(regionId);
        if (ObjectUtil.isNull(region)) {
            throw new ForbiddenOperationException("所属区域不存在");
        }


        boolean update = lambdaUpdate()
                .set(Serve::getSaleStatus, FoundationStatusEnum.DISABLE.getStatus())
                .eq(Serve::getId, id).update();

        if (!update) {
            throw new CommonException("服务下架失败");
        }
        return serve;
    }

    @Override
    public void offHot(Long id) {

        Serve serve = baseMapper.selectById(id);

        if (serve == null) {
            throw new CommonException("服务不存在");
        }

        //服务的状态必须为已上架
        if (serve.getSaleStatus() != FoundationStatusEnum.ENABLE.getStatus()) {
            throw new ForbiddenOperationException("只有已上架状态才能设置热门");
        }

        //服务必须得是热门
        if (serve.getIsHot() != 1) {
            throw new ForbiddenOperationException("只有热门状态才能取消热门");
        }

        boolean update = lambdaUpdate()
                .set(Serve::getIsHot, 0)
                .eq(Serve::getId, id).update();

        if (!update) {
            throw new CommonException("取消服务热门失败");
        }
    }

    @Override
    public void onHot(Long id) {

        Serve serve = baseMapper.selectById(id);

        if (serve == null) {
            throw new CommonException("服务不存在");
        }

        //服务的状态必须为已上架
        if (serve.getSaleStatus() != FoundationStatusEnum.ENABLE.getStatus()) {
            throw new ForbiddenOperationException("只有已上架状态才能设置热门");
        }

        //服务必须得是非热门
        if (serve.getIsHot() != 0) {
            throw new ForbiddenOperationException("只有非热门状态才能设置热门");
        }

        boolean update = lambdaUpdate()
                .set(Serve::getIsHot, 1)
                .set(Serve::getHotTimeStamp, System.currentTimeMillis())
                .eq(Serve::getId, id).update();

        if (!update) {
            throw new CommonException("设置服务热门失败");
        }
    }

    @Override
    @Cacheable(value = RedisConstants.CacheName.SERVE,key="#id",cacheManager = RedisConstants.CacheManager.ONE_DAY)
    public Serve queryServeByIdCache(Long id) {
        return getById(id);
    }

    @Override
    public List<Serve> queryHotAndOnSaleServeList() {
        return lambdaQuery()
                .eq(Serve::getSaleStatus, FoundationStatusEnum.ENABLE.getStatus())
                .eq(Serve::getIsHot, 1)
                .list();
    }

    // @Override
    // public ServeAggregationSimpleResDTO findDetailById(Long id) {
    //     return baseMapper.findServeDetailById(id);
    // }


    @Override
    public ServeAggregationSimpleResDTO findById(Long id) {
        return baseMapper.findById(id);
    }

    @Override
    public ServeAggregationResDTO findServeDetailById(Long id) {
        return baseMapper.findServeDetailById(id);
    }
}