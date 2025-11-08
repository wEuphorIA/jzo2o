package com.jzo2o.customer.service;

import com.jzo2o.customer.model.domain.UserBankAccount;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.customer.model.dto.request.BankAccountUpsertReqDTO;
import com.jzo2o.customer.model.dto.response.BankAccountResDTO;

/**
* @author Euphoria
* @description 针对表【user_bank_account(用户银行账户信息表)】的数据库操作Service
* @createDate 2025-11-08 17:28:03
*/
public interface UserBankAccountService extends IService<UserBankAccount> {

    BankAccountResDTO addOrUpdate(BankAccountUpsertReqDTO bankAccountUpsertReqDTO);
}
