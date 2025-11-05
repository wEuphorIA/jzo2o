package com.jzo2o.mysql.interceptor;

import com.jzo2o.common.model.CurrentUser;
import com.jzo2o.common.utils.ReflectUtils;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.jzo2o.common.utils.UserContext;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;

import static com.jzo2o.mysql.constants.DbFiledConstants.*;

/**
 * @author itcast
 */
public class MyBatisAutoFillInterceptor implements InnerInterceptor {

//    private final UserInfoHandler userInfoHandler;
//
//    public MyBatisAutoFillInterceptor(UserInfoHandler userInfoHandler) {
//        this.userInfoHandler = userInfoHandler;
//    }

    @Override
    public void beforeUpdate(Executor executor, MappedStatement ms, Object parameter) throws SQLException {
        //1.更新操作
        updateExe(parameter);
        //2.插入操作
        insertExe(ms, parameter);
    }

    private void insertExe(MappedStatement ms, Object parameter){
        //1.判断当前操作是否是插入操作
        if(ms.getSqlCommandType().compareTo(SqlCommandType.INSERT) == 0) {
            //2.判断是否有updater字段，如果
            if(ReflectUtils.containField(CREATE_BY, parameter.getClass())){

                //3.有userId也存在并设置updater
                if(UserContext.currentUser() != null){
                    //4.当前操作人设置到创建人字段
                    ReflectUtils.setFieldValue(parameter, CREATE_BY, currentUserId());

                }
            }
            if(ReflectUtils.containField(UPDATE_BY, parameter.getClass())){

                //3.有userId也存在并设置updater
                if(UserContext.currentUser() != null){
                    //4.当前操作人设置到创建人字段
                    ReflectUtils.setFieldValue(parameter, UPDATE_BY, currentUserId());

                }
            }
            //填充创建时间
            if(ReflectUtils.containField(CREATE_TIME, parameter.getClass())){
                ReflectUtils.setFieldValue(parameter, CREATE_TIME, LocalDateTime.now());
            }
            if(ReflectUtils.containField(UPDATE_TIME, parameter.getClass())){
                ReflectUtils.setFieldValue(parameter, UPDATE_TIME, LocalDateTime.now());
            }
        }
    }

    private void updateExe(Object parameter){

        //更新时的参数可能是一个map,map的key为et,et是在BaseMapper中指定的
        if(parameter instanceof Map && ((Map) parameter).containsKey("et")){
            parameter = ((Map) parameter).get("et");
            if(parameter == null)
                return ;
        }else{
            return ;
        }
        //1.判断是否有updater字段
        if(ReflectUtils.containField(UPDATE_BY, parameter.getClass())){
            String userId = currentUserId();
            //2.如果有userId也存在并设置updater
            if(userId != null){
                //3.当前用户设置到更新人字段
                ReflectUtils.setFieldValue(parameter, UPDATE_BY, userId);

            }

        }
        //填充修改时间
        if(ReflectUtils.containField(UPDATE_TIME, parameter.getClass())){
            ReflectUtils.setFieldValue(parameter, UPDATE_TIME, LocalDateTime.now());
        }
    }

    private String currentUserId() {
        CurrentUser currentUserInfo = UserContext.currentUser();
        return currentUserInfo != null ? currentUserInfo.getIdString() : null;
    }
}
