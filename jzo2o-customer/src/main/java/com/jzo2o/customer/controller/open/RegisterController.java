package com.jzo2o.customer.controller.open;

import com.jzo2o.customer.model.dto.request.InstitutionRegisterReqDTO;
import com.jzo2o.customer.service.ILoginService;
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
 @date 2025/11/7 下午7:48 */
@RestController("registerController")
@Api(tags = "白名单接口 - 客户注册相关接口")
@RequestMapping("/open/serve-provider/institution")
public class RegisterController {

    @Resource
    private ILoginService loginService;


    @PostMapping("/register")
    @ApiOperation("机构注册接口")
    public void register(@RequestBody InstitutionRegisterReqDTO institutionRegisterReqDTO){
        loginService.register(institutionRegisterReqDTO);
    }

}
