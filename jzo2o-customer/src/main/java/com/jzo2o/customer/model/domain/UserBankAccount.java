package com.jzo2o.customer.model.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 用户银行账户信息表
 * @TableName user_bank_account
 */
@TableName(value ="user_bank_account")
@Data
public class UserBankAccount {
    /**
     * 服务人员/机构id
     */
    @TableId
    private Long id;

    /**
     * 类型：2-服务人员，3-服务机构
     */
    private Integer type;

    /**
     * 名称
     */
    private String name;

    /**
     * 银行名称
     */
    private String bankName;

    /**
     * 网点
     */
    private String branch;

    /**
     * 银行账号
     */
    private String account;

    /**
     * 省
     */
    private String province;

    /**
     * 市
     */
    private String city;

    /**
     * 区
     */
    private String district;

    /**
     * 开户证明
     */
    private String accountCertification;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}