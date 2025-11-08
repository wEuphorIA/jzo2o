package com.jzo2o.customer.controller.agency;

import com.jzo2o.common.utils.UserContext;
import com.jzo2o.customer.model.domain.AgencyCertification;
import com.jzo2o.customer.model.dto.request.AgencyCertificationAuditAddReqDTO;
import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditAddReqDTO;
import com.jzo2o.customer.model.dto.response.RejectReasonResDTO;
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
@RestController("agencyAuthRealNameController")
@RequestMapping("/agency/agency-certification-audit")
@Api("机构端提交认证")
public class AuthRealNameController {

    @Resource
    private IAgencyCertificationService agencyCertificationService;

    @ApiOperation("机构提交认证申请")
    @PostMapping
    public void workerCertificationAudit(@RequestBody AgencyCertificationAuditAddReqDTO agencyCertificationAuditAddReqDTO) {
        agencyCertificationService.workerCertificationAudit(agencyCertificationAuditAddReqDTO);
    }

    @ApiOperation("查询最新的驳回原因")
    @GetMapping("/rejectReason")
    public RejectReasonResDTO rejectReason() {
        AgencyCertification certification = agencyCertificationService.getById(UserContext.currentUserId());
        return new RejectReasonResDTO(certification.getRejectReason());
    }
}
