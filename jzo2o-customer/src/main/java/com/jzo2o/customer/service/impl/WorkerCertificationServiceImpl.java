package com.jzo2o.customer.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.customer.enums.CertificationStatusEnum;
import com.jzo2o.customer.mapper.WorkerCertificationMapper;
import com.jzo2o.customer.model.domain.ServeProvider;
import com.jzo2o.customer.model.domain.WorkerCertification;
import com.jzo2o.customer.model.dto.WorkerCertificationUpdateDTO;
import com.jzo2o.customer.model.dto.request.CertificationAuditReqDTO;
import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditAddReqDTO;
import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditPageQueryReqDTO;
import com.jzo2o.customer.model.dto.response.WorkerCertificationAuditResDTO;
import com.jzo2o.customer.service.IServeProviderService;
import com.jzo2o.customer.service.IWorkerCertificationService;
import com.jzo2o.mysql.utils.PageHelperUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static com.jzo2o.customer.enums.CertificationStatusEnum.PROGRESSING;

/**
 * <p>
 * 服务人员认证信息表 服务实现类
 * </p>
 *
 * @author itcast
 * @since 2023-09-06
 */
@Service
@Slf4j
public class WorkerCertificationServiceImpl extends ServiceImpl<WorkerCertificationMapper, WorkerCertification> implements IWorkerCertificationService {


    @Resource
    private IServeProviderService serveProviderService;


    /**
     * 根据服务人员id更新
     *
     * @param workerCertificationUpdateDTO 服务人员认证更新模型
     */
    @Override
    public void updateById(WorkerCertificationUpdateDTO workerCertificationUpdateDTO) {
        LambdaUpdateWrapper<WorkerCertification> updateWrapper = Wrappers.<WorkerCertification>lambdaUpdate()
                .eq(WorkerCertification::getId, workerCertificationUpdateDTO.getId())
                .set(WorkerCertification::getCertificationStatus, workerCertificationUpdateDTO.getCertificationStatus())
                .set(ObjectUtil.isNotEmpty(workerCertificationUpdateDTO.getName()), WorkerCertification::getName, workerCertificationUpdateDTO.getName())
                .set(ObjectUtil.isNotEmpty(workerCertificationUpdateDTO.getIdCardNo()), WorkerCertification::getIdCardNo, workerCertificationUpdateDTO.getIdCardNo())
                .set(ObjectUtil.isNotEmpty(workerCertificationUpdateDTO.getFrontImg()), WorkerCertification::getFrontImg, workerCertificationUpdateDTO.getFrontImg())
                .set(ObjectUtil.isNotEmpty(workerCertificationUpdateDTO.getBackImg()), WorkerCertification::getBackImg, workerCertificationUpdateDTO.getBackImg())
                .set(ObjectUtil.isNotEmpty(workerCertificationUpdateDTO.getCertificationMaterial()), WorkerCertification::getCertificationMaterial, workerCertificationUpdateDTO.getCertificationMaterial())
                .set(ObjectUtil.isNotEmpty(workerCertificationUpdateDTO.getCertificationTime()), WorkerCertification::getCertificationTime, workerCertificationUpdateDTO.getCertificationTime());
        super.update(updateWrapper);
    }

    @Override
    @Transactional
    public void workerCertificationAudit(WorkerCertificationAuditAddReqDTO workerCertificationAuditAddReqDTO) {
        // 1. 查询现有记录
        Long userId = UserContext.currentUserId();
        WorkerCertification existing = baseMapper.selectById(userId);
        WorkerCertification saveEntity;

        if (existing == null) {
            // 新增逻辑
            saveEntity = new WorkerCertification();
            saveEntity.setId(userId);
            saveEntity.setCertificationStatus(CertificationStatusEnum.PROGRESSING.getStatus());
            log.info("新增人员认证信息, userId: {}", userId);
        } else {
            // 更新逻辑：基于现有对象
            saveEntity = existing;
            saveEntity.setCertificationStatus(PROGRESSING.getStatus());
            saveEntity.setAuditStatus(0);
            saveEntity.setRejectReason("");
            log.info("更新人员认证信息, userId: {}", userId);
        }

        // 2. DTO数据拷贝（不覆盖关键字段）
        BeanUtils.copyProperties(workerCertificationAuditAddReqDTO, saveEntity);

        this.saveOrUpdate(saveEntity);
    }

    @Override
    public PageResult<WorkerCertificationAuditResDTO> workerPage(WorkerCertificationAuditPageQueryReqDTO workerCertificationAuditPageQueryReqDTO) {
        return PageHelperUtils.selectPage(workerCertificationAuditPageQueryReqDTO,
                () -> baseMapper.queryList(workerCertificationAuditPageQueryReqDTO));
    }

    @Override
    public void workerAudit(String id, CertificationAuditReqDTO certificationAuditReqDTO) {
        WorkerCertification workerCertification = baseMapper.selectById(id);

        if (workerCertification == null) {
            throw new RuntimeException("人员认证信息不存在");
        }
        //打印当前操作人
        log.info("当前操作人: {}", UserContext.currentUserId());

        if (certificationAuditReqDTO.getCertificationStatus() == CertificationStatusEnum.SUCCESS.getStatus()){
            workerCertification.setCertificationTime(LocalDateTime.now());
            ServeProvider serveProvider = serveProviderService.getById(workerCertification.getId());
            serveProvider.setSettingsStatus(1);
            serveProviderService.updateById(serveProvider);
        }else {
            workerCertification.setRejectReason(certificationAuditReqDTO.getRejectReason());
        }
        workerCertification.setAuditStatus(1);
        workerCertification.setCertificationStatus(certificationAuditReqDTO.getCertificationStatus());
        workerCertification.setAuditTime(LocalDateTime.now());
        workerCertification.setAuditorId(UserContext.currentUserId());
        workerCertification.setAuditorName(UserContext.getCurrentUser().getName());
        workerCertification.setServeProviderId(UserContext.currentUserId());
        this.updateById(workerCertification);
    }
}
