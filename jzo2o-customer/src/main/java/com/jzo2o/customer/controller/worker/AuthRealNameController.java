package com.jzo2o.customer.controller.worker;

import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditAddReqDTO;
import com.jzo2o.customer.model.dto.response.RejectReasonResDTO;
import com.jzo2o.customer.service.IWorkerCertificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 @author Euphoria
 @version 1.0
 @description: TODO
 @date 2025/11/7 下午8:59 */
@RestController("workerAuthRealNameController")
@RequestMapping("/worker/worker-certification-audit")
@Api("服务端提交认证接口")
public class AuthRealNameController {

    @Resource
    private IWorkerCertificationService workerCertificationService;

    @ApiOperation("服务端提交认证申请")
    @PostMapping
    public void workerCertificationAudit(@RequestBody WorkerCertificationAuditAddReqDTO workerCertificationAuditAddReqDTO) {

    }

    @ApiOperation("查询最新的驳回原因")
    @PostMapping("/rejectReason")
    public RejectReasonResDTO rejectReason() {
        return null;
    }
}
