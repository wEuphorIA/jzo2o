package com.jzo2o.customer.controller.worker;

import cn.hutool.core.bean.BeanUtil;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.customer.model.dto.request.BankAccountUpsertReqDTO;
import com.jzo2o.customer.model.dto.response.BankAccountResDTO;
import com.jzo2o.customer.service.UserBankAccountService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 @author Euphoria
 @version 1.0
 @description: TODO
 @date 2025/11/8 下午5:33 */
@RestController("workerBankAccountController")
@RequestMapping("/worker/bank-account")
@Api("机构端设置银行账户")
public class BankAccountController {

    @Resource
    private UserBankAccountService userBankAccountService;

    @PostMapping
    public BankAccountResDTO addOrUpdate(@RequestBody BankAccountUpsertReqDTO bankAccountUpsertReqDTO){
        bankAccountUpsertReqDTO.setType(2);
        return userBankAccountService.addOrUpdate(bankAccountUpsertReqDTO);
    }

    @GetMapping("/currentUserBankAccount")
    public BankAccountResDTO currentUserBankAccount(){
        return BeanUtil.toBean(userBankAccountService.getById(UserContext.currentUserId()), BankAccountResDTO.class);
    }
}
