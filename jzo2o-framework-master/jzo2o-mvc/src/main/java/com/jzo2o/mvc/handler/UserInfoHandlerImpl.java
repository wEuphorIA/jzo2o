package com.jzo2o.mvc.handler;

import com.jzo2o.common.handler.UserInfoHandler;
import com.jzo2o.common.model.CurrentUser;
import com.jzo2o.common.utils.UserContext;
import org.springframework.stereotype.Component;

@Component
public class UserInfoHandlerImpl implements UserInfoHandler {

    @Override
    public CurrentUser currentUserInfo() {
        return UserContext.getCurrentUser();
    }
}
