package com.jzo2o.mvc.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jzo2o.common.model.Result;
import com.jzo2o.common.utils.IoUtils;
import com.jzo2o.mvc.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.*;
import java.nio.charset.StandardCharsets;

import static com.jzo2o.mvc.constants.HeaderConstants.BODY_PROCESSED;

/**
 * 用于包装外网访问
 */
@Component
@Slf4j
public class PackResultFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        // 1.无需包装，放过拦截
        String requestURI = ((HttpServletRequest) servletRequest).getRequestURI();
        if (requestURI.contains(".") ||
                requestURI.contains("/swagger") ||
                requestURI.contains("/api-docs") ||
                requestURI.contains("/inner")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        // 2.包装响应值
        // 2.1.处理业务，获取响应值
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        ResponseWrapper responseWrapper = new ResponseWrapper(response);
        filterChain.doFilter(servletRequest, responseWrapper);
        // 无需包装
        if (response.containsHeader(BODY_PROCESSED) && response.getHeader(BODY_PROCESSED).equals("1")) {
            IoUtils.write(response.getOutputStream(), false, responseWrapper.getResponseData());
            return;
        }

        // 2.2.包装
        byte[] bytes = Result.plainOk(responseWrapper.getResponseData());

//        byte[] bytes = capturedResponse.getBytes(StandardCharsets.UTF_8);
//        byte[] bytes = "{\"code\":200,\"msg\":\"OK\",\"data\":100}".getBytes(StandardCharsets.UTF_8);
        log.info("result : {}", new String(bytes));
        // 2.3.写入
        response.setContentType("application/json;charset=UTF-8");
        response.setContentLength(bytes.length);
        IoUtils.write(response.getOutputStream(), false, bytes);
//        response.getWriter().write(new String(bytes, StandardCharsets.UTF_8));
//        response.getWriter().flush();
    }

}
