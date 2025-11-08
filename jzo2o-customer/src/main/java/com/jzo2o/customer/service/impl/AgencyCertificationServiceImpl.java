package com.jzo2o.customer.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.customer.enums.CertificationStatusEnum;
import com.jzo2o.customer.mapper.AgencyCertificationMapper;
import com.jzo2o.customer.model.domain.AgencyCertification;
import com.jzo2o.customer.model.domain.ServeProvider;
import com.jzo2o.customer.model.dto.AgencyCertificationUpdateDTO;
import com.jzo2o.customer.model.dto.request.AgencyCertificationAuditAddReqDTO;
import com.jzo2o.customer.model.dto.request.AgencyCertificationAuditPageQueryReqDTO;
import com.jzo2o.customer.model.dto.request.CertificationAuditReqDTO;
import com.jzo2o.customer.model.dto.response.AgencyCertificationAuditResDTO;
import com.jzo2o.customer.service.IAgencyCertificationService;
import com.jzo2o.customer.service.IServeProviderService;
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
 * 机构认证信息表 服务实现类
 * </p>
 *
 * @author itcast
 * @since 2023-09-06
 */
@Service
@Slf4j
public class AgencyCertificationServiceImpl extends ServiceImpl<AgencyCertificationMapper, AgencyCertification> implements IAgencyCertificationService {


    @Resource
    private IServeProviderService serveProviderService;

    /**
     * 根据机构id更新
     *
     * @param agencyCertificationUpdateDTO 机构认证更新模型
     */
    @Override
    public void updateByServeProviderId(AgencyCertificationUpdateDTO agencyCertificationUpdateDTO) {
        LambdaUpdateWrapper<AgencyCertification> updateWrapper = Wrappers.<AgencyCertification>lambdaUpdate()
                .eq(AgencyCertification::getId, agencyCertificationUpdateDTO.getId())
                .set(AgencyCertification::getCertificationStatus, agencyCertificationUpdateDTO.getCertificationStatus())
                .set(ObjectUtil.isNotEmpty(agencyCertificationUpdateDTO.getName()), AgencyCertification::getName, agencyCertificationUpdateDTO.getName())
                .set(ObjectUtil.isNotEmpty(agencyCertificationUpdateDTO.getIdNumber()), AgencyCertification::getIdNumber, agencyCertificationUpdateDTO.getIdNumber())
                .set(ObjectUtil.isNotEmpty(agencyCertificationUpdateDTO.getLegalPersonName()), AgencyCertification::getLegalPersonName, agencyCertificationUpdateDTO.getLegalPersonName())
                .set(ObjectUtil.isNotEmpty(agencyCertificationUpdateDTO.getLegalPersonIdCardNo()), AgencyCertification::getLegalPersonIdCardNo, agencyCertificationUpdateDTO.getLegalPersonIdCardNo())
                .set(ObjectUtil.isNotEmpty(agencyCertificationUpdateDTO.getBusinessLicense()), AgencyCertification::getBusinessLicense, agencyCertificationUpdateDTO.getBusinessLicense())
                .set(ObjectUtil.isNotEmpty(agencyCertificationUpdateDTO.getCertificationTime()), AgencyCertification::getCertificationTime, agencyCertificationUpdateDTO.getCertificationTime());
        super.update(updateWrapper);
    }

    @Override
    @Transactional
    public void agencyCertificationAudit(AgencyCertificationAuditAddReqDTO agencyCertificationAuditAddReqDTO) {
        // 1. 查询现有记录
        Long userId = UserContext.currentUserId();
        AgencyCertification existing = baseMapper.selectById(userId);
        AgencyCertification saveEntity;

        if (existing == null) {
            // 新增逻辑
            saveEntity = new AgencyCertification();
            saveEntity.setId(userId);
            saveEntity.setCertificationStatus(CertificationStatusEnum.PROGRESSING.getStatus());
            log.info("新增机构认证信息, userId: {}", userId);
        } else {
            // 更新逻辑：基于现有对象
            saveEntity = existing;
            saveEntity.setCertificationStatus(PROGRESSING.getStatus());
            saveEntity.setAuditStatus(0);
            saveEntity.setRejectReason("");
            log.info("更新机构认证信息, userId: {}", userId);
        }

        // 2. DTO数据拷贝（不覆盖关键字段）
        BeanUtils.copyProperties(agencyCertificationAuditAddReqDTO, saveEntity);

        this.saveOrUpdate(saveEntity);
    }

    @Override
    public PageResult<AgencyCertificationAuditResDTO> agencyPage(AgencyCertificationAuditPageQueryReqDTO agencyCertificationAuditPageQueryReqDTO) {
        return PageHelperUtils.selectPage(agencyCertificationAuditPageQueryReqDTO,
                () -> baseMapper.queryList(agencyCertificationAuditPageQueryReqDTO));
    }

    @Override
    @Transactional
    public void agencyAudit(String id, CertificationAuditReqDTO certificationAuditReqDTO) {
        AgencyCertification agencyCertification = baseMapper.selectById(id);

        if (agencyCertification == null) {
            throw new RuntimeException("机构认证信息不存在");
        }
        //打印当前操作人
        log.info("当前操作人: {}", UserContext.currentUserId());

        if (certificationAuditReqDTO.getCertificationStatus() == CertificationStatusEnum.SUCCESS.getStatus()){
            agencyCertification.setCertificationTime(LocalDateTime.now());
            ServeProvider serveProvider = serveProviderService.getById(agencyCertification.getId());
            serveProvider.setSettingsStatus(1);
            serveProviderService.updateById(serveProvider);
        }else {
            agencyCertification.setRejectReason(certificationAuditReqDTO.getRejectReason());
        }
        agencyCertification.setAuditStatus(1);
        agencyCertification.setCertificationStatus(certificationAuditReqDTO.getCertificationStatus());
        agencyCertification.setAuditTime(LocalDateTime.now());
        agencyCertification.setAuditorId(UserContext.currentUserId());
        agencyCertification.setAuditorName(UserContext.getCurrentUser().getName());
        agencyCertification.setServeProviderId(UserContext.currentUserId());
        this.updateById(agencyCertification);
    }
}
