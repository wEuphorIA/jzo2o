package com.jzo2o.api.customer;

import com.jzo2o.api.customer.dto.response.AddressBookResDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 内部接口 - 地址薄相关接口
 *
 * @author itcast
 */
@FeignClient(contextId = "jzo2o-customer", value = "jzo2o-customer", path = "/customer/inner/address-book")
public interface AddressBookApi {

    @GetMapping("/{id}")
    AddressBookResDTO detail(@PathVariable("id") Long id);

    /**
     * 根据用户id和城市编码获取用户地址列表
     * @param userId 用户id
     * @param city 城市名称
     * @return 用户服务地址列表
     */
    @GetMapping("/getByUserIdAndCity")
    @ApiOperation("根据用户id和城市名称，取用户地址列表")
    List<AddressBookResDTO> getByUserIdAndCity(@RequestParam("userId") Long userId, @RequestParam("city") String city);
}
