package com.jzo2o.common.model;

/**
 * 当前用户信息
 */
//@Data
//@AllArgsConstructor
public interface CurrentUser {


    /**
     * 当前用户id
     */
    public String getIdString();

    public Long getId();

    /**
     * 用户名/昵称
     */
    public  String getName();

    public void setName(String name);
    /**
     * 头像
     */
    public String getAvatar();
    /**
     * 用户类型
     */
    public Integer getUserType();
}
