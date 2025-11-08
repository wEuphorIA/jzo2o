package com.jzo2o.customer.mapper;

import com.jzo2o.customer.model.domain.WorkerCertification;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditPageQueryReqDTO;
import com.jzo2o.customer.model.dto.response.WorkerCertificationAuditResDTO;

import java.util.List;

/**
 * <p>
 * 服务人员认证信息表 Mapper 接口
 * </p>
 *
 * @author itcast
 * @since 2023-09-06
 */
public interface WorkerCertificationMapper extends BaseMapper<WorkerCertification> {

    List<WorkerCertificationAuditResDTO> queryList(WorkerCertificationAuditPageQueryReqDTO workerCertificationAuditPageQueryReqDTO);
}
