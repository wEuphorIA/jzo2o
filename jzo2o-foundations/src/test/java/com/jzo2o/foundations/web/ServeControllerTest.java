package com.jzo2o.foundations.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author Mr.M
 * @version 1.0
 * @description ServeController单元测试类
 * @date 2024/9/14 11:07
 */

@SpringBootTest
@AutoConfigureMockMvc
public class ServeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    public void doTest() throws Exception {
        mockMvc.perform(get("/operation/serve/page")
                .param("regionId", "1693814923234189313")
        ).andExpect(status().isOk()).andDo(result -> {
            //解析响应结果
            String contentAsString = result.getResponse().getContentAsString();
            System.out.println(contentAsString);
        });
    }
}