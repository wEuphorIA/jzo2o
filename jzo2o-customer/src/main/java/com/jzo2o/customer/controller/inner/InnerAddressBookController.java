package com.jzo2o.customer.controller.inner;

import cn.hutool.core.bean.BeanUtil;
import com.jzo2o.api.customer.AddressBookApi;
import com.jzo2o.api.customer.dto.response.AddressBookResDTO;
import com.jzo2o.customer.model.domain.AddressBook;
import com.jzo2o.customer.service.IAddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 内部接口 - 地址薄相关接口
 *
 * @author itcast
 * @create 2023/7/10 09:21
 **/
@RestController
@RequestMapping("inner/address-book")
@Api(tags = "内部接口 - 地址薄相关接口")
public class InnerAddressBookController implements AddressBookApi {
    @Resource
    private IAddressBookService addressBookService;

    @Override
    @GetMapping("/{id}")
    @ApiOperation("地址薄详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "地址薄id", required = true, dataTypeClass = Long.class)
    })
    public AddressBookResDTO detail(@PathVariable("id") Long id) {
        AddressBook addressBook = addressBookService.getById(id);
        return BeanUtil.toBean(addressBook, AddressBookResDTO.class);
    }

    @Override
    @GetMapping("/getByUserIdAndCity")
    @ApiOperation("根据用户id和城市获取用户地址列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", required = true, dataTypeClass = Long.class),
            @ApiImplicitParam(name = "city", value = "城市名称", required = true, dataTypeClass = String.class)

    })
    public List<AddressBookResDTO> getByUserIdAndCity(@RequestParam("userId") Long userId,@RequestParam("city") String city) {
        return addressBookService.getByUserIdAndCity(userId, city);
    }
}
