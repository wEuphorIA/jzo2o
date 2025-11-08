package com.jzo2o.customer.mapper;

import com.jzo2o.customer.model.domain.AgencyCertification;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jzo2o.customer.model.dto.request.AgencyCertificationAuditPageQueryReqDTO;
import com.jzo2o.customer.model.dto.response.AgencyCertificationAuditResDTO;

import java.util.List;

/**
 * <p>
 * 机构认证信息表 Mapper 接口
 * </p>
 *
 * @author itcast
 * @since 2023-09-06
 */
public interface AgencyCertificationMapper extends BaseMapper<AgencyCertification> {

    List<AgencyCertificationAuditResDTO> queryList(AgencyCertificationAuditPageQueryReqDTO agencyCertificationAuditPageQueryReqDTO);
}
