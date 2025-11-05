package com.jzo2o.mysql.interceptor;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.jzo2o.common.model.CurrentUser;
import com.jzo2o.common.utils.UserContext;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * 字段填充
 *
 */
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        CurrentUser currentUserInfo = UserContext.currentUser();
        if(metaObject.hasSetter("createBy")){
            Class<?> getterType = metaObject.getGetterType("createBy");
            if (getterType.equals(String.class)) {
                if (currentUserInfo != null ) {
                    this.setFieldValByName("createBy", currentUserInfo.getName(), metaObject);
                } else {
                    this.setFieldValByName("createBy", "SYSTEM", metaObject);
                }
            }
        }

        //有创建时间字段，切字段值为空
        if (metaObject.hasGetter("createTime")) {
            this.setFieldValByName("createTime", LocalDateTime.now(), metaObject);
        }
        //有值，则写入
        if (metaObject.hasGetter("deleteFlag")) {
            this.setFieldValByName("deleteFlag", false, metaObject);
        }
//        if (metaObject.hasGetter("id")) {
//            //如果已经配置id，则不再写入
//            if (metaObject.getValue("id") == null) {
//                this.setFieldValByName("id", String.valueOf(SnowFlake.getId()), metaObject);
//            }
//        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {

        CurrentUser currentUserInfo = UserContext.currentUser();
        if(metaObject.hasSetter("updateBy")) {
            Class<?> getterType = metaObject.getGetterType("updateBy");
            if (getterType.equals(String.class)) {
                if (currentUserInfo != null) {
                    this.setFieldValByName("updateBy", currentUserInfo.getName(), metaObject);
                }else{
                    this.setFieldValByName("updateBy", "SYSTEM", metaObject);
                }
            }
        }
        if (metaObject.hasGetter("updateTime")) {
            this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
        }

    }
}

