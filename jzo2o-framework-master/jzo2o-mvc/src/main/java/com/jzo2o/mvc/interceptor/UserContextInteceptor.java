package com.jzo2o.mvc.interceptor;

import com.jzo2o.common.constants.HeaderConstants;
import com.jzo2o.common.model.CurrentUserInfo;
import com.jzo2o.common.utils.Base64Utils;
import com.jzo2o.common.utils.JsonUtils;
import com.jzo2o.common.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author itcast
 */
@Slf4j
public class UserContextInteceptor implements HandlerInterceptor {

//    @Bean
//    public UserContext<CurrentUserInfo> getUserContext() {
//        return new UserContext<CurrentUserInfo>();
//    }
//
//    @Autowired
//    UserContext<CurrentUserInfo> userContext;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.尝试获取头信息中的用户信息
        String userInfo = request.getHeader(HeaderConstants.USER_INFO);
        // 2.判断是否为空
        if (userInfo == null) {
            return true;
        }
        try {
            // 3.base64解码用户信息
            String decodeUserInfo = Base64Utils.decodeStr(userInfo);
            CurrentUserInfo currentUserInfo = JsonUtils.toBean(decodeUserInfo, CurrentUserInfo.class);

            // 4.将用户信息保存到threadlocal中
            UserContext.set(currentUserInfo);
            return true;
        } catch (NumberFormatException e) {
            log.error("用户身份信息格式不正确，{}, 原因：{}", userInfo, e.getMessage());
            return true;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理用户信息
        UserContext.clear();
    }
}
