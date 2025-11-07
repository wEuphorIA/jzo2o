package com.jzo2o.foundations.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.expcetions.CommonException;
import com.jzo2o.common.expcetions.ForbiddenOperationException;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.common.utils.BeanUtils;
import com.jzo2o.foundations.enums.FoundationStatusEnum;
import com.jzo2o.foundations.mapper.RegionMapper;
import com.jzo2o.foundations.mapper.ServeItemMapper;
import com.jzo2o.foundations.mapper.ServeMapper;
import com.jzo2o.foundations.model.domain.Region;
import com.jzo2o.foundations.model.domain.Serve;
import com.jzo2o.foundations.model.domain.ServeItem;
import com.jzo2o.foundations.model.dto.request.ServePageQueryReqDTO;
import com.jzo2o.foundations.model.dto.request.ServeUpsertReqDTO;
import com.jzo2o.foundations.model.dto.response.ServeResDTO;
import com.jzo2o.foundations.service.IServeService;
import com.jzo2o.mysql.utils.PageHelperUtils;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayDeque;
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
    public void onSale(Long id) {
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

        boolean update = lambdaUpdate()
                .set(Serve::getSaleStatus, FoundationStatusEnum.ENABLE.getStatus())
                .eq(Serve::getId, id).update();

        if (!update) {
            throw new CommonException("服务上架失败");
        }
    }
}