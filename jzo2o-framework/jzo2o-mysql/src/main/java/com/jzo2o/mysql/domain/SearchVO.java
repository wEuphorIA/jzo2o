package com.jzo2o.mysql.domain;

import cn.hutool.core.date.DateUtil;
import com.jzo2o.common.utils.DateUtils;
import com.jzo2o.common.utils.StringUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期搜索参数
 *
 * @author Chopper
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchVO implements Serializable {

    @ApiModelProperty(value = "起始日期")
    private String startDate;

    @ApiModelProperty(value = "结束日期")
    private String endDate;

    public Date getConvertStartDate() {
        if (StringUtils.isEmpty(startDate)) {
            return null;
        }
        return DateUtils.toDate(startDate, DateUtils.DEFAULT_DATE_FORMAT);
    }

    public Date getConvertEndDate() {
        if (StringUtils.isEmpty(endDate)) {
            return null;
        }
        //结束时间等于结束日期+1天 -1秒，
        Date date = DateUtils.toDate(endDate, DateUtils.DEFAULT_DATE_FORMAT);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
        calendar.set(Calendar.SECOND, -1);
        return calendar.getTime();
    }
}
