package com.jzo2o.customer.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.customer.model.domain.UserBankAccount;
import com.jzo2o.customer.model.dto.request.BankAccountUpsertReqDTO;
import com.jzo2o.customer.model.dto.response.BankAccountResDTO;
import com.jzo2o.customer.service.UserBankAccountService;
import com.jzo2o.customer.mapper.UserBankAccountMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* @author Euphoria
* @description 针对表【user_bank_account(用户银行账户信息表)】的数据库操作Service实现
* @createDate 2025-11-08 17:28:03
*/
@Service
public class UserBankAccountServiceImpl extends ServiceImpl<UserBankAccountMapper, UserBankAccount>
    implements UserBankAccountService{

    @Override
    @Transactional
    public BankAccountResDTO addOrUpdate(BankAccountUpsertReqDTO bankAccountUpsertReqDTO) {

        if (bankAccountUpsertReqDTO.getId() == null){
            bankAccountUpsertReqDTO.setId(UserContext.currentUserId());
        }
        UserBankAccount userBankAccount = BeanUtil.toBean(bankAccountUpsertReqDTO, UserBankAccount.class);

        saveOrUpdate(userBankAccount);

        return BeanUtil.toBean(userBankAccount, BankAccountResDTO.class);
    }
}




