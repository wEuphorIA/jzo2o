package com.jzo2o.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 当前用户信息
 */
@AllArgsConstructor
public class CurrentUserInfo implements Serializable,CurrentUser {
    /**
     * 当前用户id
     */
    private Long id;
    /**
     * 用户名/昵称
     */
    private String name;
    /**
     * 头像
     */
    private String avatar;

    /**
     * 用户类型
     */
    private Integer userType;

    @Override
    public String getIdString() {
        return String.valueOf(id);
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @Override
    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }
}
