package com.jzo2o.mvc.handler;

import cn.hutool.core.util.IdUtil;
import com.jzo2o.common.constants.HeaderConstants;
import com.jzo2o.common.handler.RequestIdHandler;
import com.jzo2o.common.model.CurrentUser;
import com.jzo2o.common.utils.StringUtils;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mvc.utils.RequestUtils;
import org.springframework.stereotype.Component;

/**
 * @author itcast
 */
@Component
public class RequestIdHandlerImpl implements RequestIdHandler {


    @Override
    public String getRequestId() {
        // 从请求header头中获取请求id,获取不到id，生成新的请求id
        CurrentUser currentUserInfo = UserContext.getCurrentUser();
        if(currentUserInfo == null) {
            return null;
        }
        String requestId = RequestUtils.getValueFromHeader(HeaderConstants.REQUEST_ID);
        if (StringUtils.isEmpty(requestId)) {
            return IdUtil.getSnowflakeNextIdStr();
        } else {
            return requestId;
        }
    }
}
