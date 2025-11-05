package com.jzo2o.mysql.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


/**
 * 数据库基础实体类
 */
@Data
@JsonIgnoreProperties(value = {"handler", "fieldHandler"})
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseIdEntity implements Serializable {

    private static final long serialVersionUID = 1L;


    @TableId
    @ApiModelProperty(value = "唯一标识", hidden = true)
    private String id;


}
