package com.jzo2o.customer.controller.operation;

import com.jzo2o.common.model.PageResult;
import com.jzo2o.customer.model.dto.request.AgencyCertificationAuditAddReqDTO;
import com.jzo2o.customer.model.dto.request.AgencyCertificationAuditPageQueryReqDTO;
import com.jzo2o.customer.model.dto.request.CertificationAuditReqDTO;
import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditPageQueryReqDTO;
import com.jzo2o.customer.model.dto.response.AgencyCertificationAuditResDTO;
import com.jzo2o.customer.model.dto.response.RejectReasonResDTO;
import com.jzo2o.customer.model.dto.response.WorkerCertificationAuditResDTO;
import com.jzo2o.customer.service.IAgencyCertificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 @author Euphoria
 @version 1.0
 @description: TODO
 @date 2025/11/7 下午8:59 */
@RestController("operationAuthRealNameController")
@RequestMapping("/operation")
@Api("运营端审核认证接口")
public class AuthRealNameController {

    @Resource
    private IAgencyCertificationService agencyCertificationService;

    @ApiOperation("审核服务人员认证分页查询")
    @GetMapping("/worker-certification-audit/page")
    public PageResult<WorkerCertificationAuditResDTO> workerPage(WorkerCertificationAuditPageQueryReqDTO workerCertificationAuditPageQueryReqDTO) {

        return null;
    }

    @ApiOperation("审核服务人员认证")
    @PutMapping("/worker-certification-audit/audit/{id}")
    public void workerAudit(@PathVariable String id, @RequestBody CertificationAuditReqDTO certificationAuditReqDTO) {

    }

    @ApiOperation("审核服务人员认证分页查询")
    @GetMapping("/agency-certification-audit/page")
    public PageResult<AgencyCertificationAuditResDTO> agencyPage(AgencyCertificationAuditPageQueryReqDTO agencyCertificationAuditPageQueryReqDTO) {

        return agencyCertificationService.agencyPage(agencyCertificationAuditPageQueryReqDTO);
    }

    @ApiOperation("审核服务人员认证")
    @PutMapping("/agency-certification-audit/audit/{id}")
    public void agencyAudit(@PathVariable String id, @RequestBody CertificationAuditReqDTO certificationAuditReqDTO) {
        agencyCertificationService.agencyAudit(id,certificationAuditReqDTO);
    }



}
