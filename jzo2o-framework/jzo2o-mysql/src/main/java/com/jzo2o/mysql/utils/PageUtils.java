package com.jzo2o.mysql.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jzo2o.common.handler.ConvertHandler;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.common.model.dto.PageQueryDTO;
import com.jzo2o.common.utils.BeanUtils;
import com.jzo2o.common.utils.CollUtils;
import com.jzo2o.common.utils.ObjectUtils;
import com.jzo2o.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jzo2o.mysql.domain.PageVO;
import com.jzo2o.mysql.domain.SearchVO;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 分页工具
 *
 * @ClassName PageUtils
 * @Author wusongsong
 * @Date 2022/6/27 17:19
 * @Version
 **/
@Slf4j
public class PageUtils {

    /**
     * mybatis的分页数据是否为空
     *
     * @param page
     * @return
     */
    public static boolean isEmpty(Page page) {
        return page == null || CollUtils.isEmpty(page.getRecords());
    }

    /**
     * 判断mybatis的分页数据不为空
     *
     * @param page
     * @return
     */
    public static boolean isNotEmpty(Page page) {
        return page != null && !CollUtils.isEmpty(page.getRecords());
    }

    /**
     * 分页数据转换，主要场景是从数据库中查出来的数据转换成DTO，或者VO
     *
     * @param originPage     从数据库查询出来的分页数据
     * @param targetClazz    目标对象class
     * @param convertHandler 数据库对象转换DTO或者VO的转换器，用于转换复杂的数据
     * @param <T>            目标对象类型
     * @param <O>            源对象类型
     * @return 用于传递的分页数据
     */
    public static <O, T> PageResult<T> toPage(Page<O> originPage, Class<T> targetClazz, ConvertHandler<O, T> convertHandler) {
        if (isEmpty(originPage)) {
            return new PageResult<>(0L, 0L, new ArrayList<>());
        }

        return new PageResult<>(originPage.getPages(), originPage.getTotal(),
                BeanUtils.copyToList(originPage.getRecords(), targetClazz, convertHandler));
    }

    /**
     * 分页数据转换返给其他微服务，主要场景是从数据库中查出来的数据转换成DTO，或者VO
     *
     * @param originPage  从数据库查询出来的分页数据
     * @param targetClazz 目标对象class
     * @param <X>         目标对象类型
     * @param <Y>         源对象类型
     * @return 用于传递的分页数据
     */
    public static <X, Y> PageResult<Y> toPage(Page<X> originPage, Class<Y> targetClazz) {
        if (isEmpty(originPage)) {
            return new PageResult<>(0L, 0L, new ArrayList<>());
        }

        return new PageResult<>(originPage.getPages(), originPage.getTotal(),
                BeanUtils.copyToList(originPage.getRecords(), targetClazz));
    }


    /**
     * 将前端传来的分页查询条件转换成数据库的查询page,
     * 如果进行排序必须填写targetClazz
     * 该方法支持简单的数据字段排序，不支持统计排序
     *
     * @param pageQueryDTO 前端传来的查询条件
     * @param <T>          查询数据库po
     * @param targetClazz  校验数据库中是否有需要排序的字段
     * @return mybatis-plus 分页查询page
     */
    public static <T> Page<T> parsePageQuery(PageQueryDTO pageQueryDTO, Class<T> targetClazz) {
        Page<T> page = new Page<>(pageQueryDTO.getPageNo(), pageQueryDTO.getPageSize());
        //是否排序
        if (targetClazz != null) {
            List orderItems = getOrderItems(pageQueryDTO, targetClazz);
            if (CollUtils.isNotEmpty(orderItems)) {
                page.addOrder(orderItems);
            }
        } else {
            //如果没有更新时间按照添加逆序排序
            OrderItem orderItem = new OrderItem();
            orderItem.setAsc(false);
            orderItem.setColumn("id");
            page.addOrder(orderItem);

        }
        return page;
    }

    public static <T> List<OrderItem> getOrderItems(PageQueryDTO pageQueryDTO, Class<T> targetClazz) {
        List<OrderItem> orderItems = new ArrayList<>();
        if (ObjectUtils.isEmpty(pageQueryDTO)) {
            return orderItems;
        }
        // 排序字段1
        if (StringUtils.isNotEmpty(pageQueryDTO.getOrderBy1())) {
            OrderItem orderItem = new OrderItem();
            orderItem.setColumn(StringUtils.toSymbolCase(pageQueryDTO.getOrderBy1(), '_'));
            orderItem.setAsc(pageQueryDTO.getIsAsc1());
            orderItems.add(orderItem);
        }
        // 排序字段2
        if (StringUtils.isNotEmpty(pageQueryDTO.getOrderBy2())) {
            OrderItem orderItem = new OrderItem();
            orderItem.setColumn(StringUtils.toSymbolCase(pageQueryDTO.getOrderBy2(), '_'));
            orderItem.setAsc(pageQueryDTO.getIsAsc2());
            orderItems.add(orderItem);
        }
        return orderItems;
    }

    public static Long pages(Long total, Long pageSize) {
        return total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
    }

    //有order by 注入风险，限制长度
    static final Integer orderByLengthLimit = 20;

    /**
     * Mybatis-Plus分页封装
     *
     * @param page 分页VO
     * @param <T>  范型
     * @return 分页响应
     */
    public static <T> Page<T> initPage(PageVO page) {

        int pageNumber = page.getPageNumber();
        int pageSize = page.getPageSize();
        String sort = page.getSort();
        String order = page.getOrder();

        if (pageNumber < 1) {
            pageNumber = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        if (pageSize > 100) {
            pageSize = 100;
        }

        Page<T> p = new Page<>(pageNumber, pageSize);

        if (CharSequenceUtil.isNotBlank(sort)) {

            if (sort.length() > orderByLengthLimit || SqlFilter.hit(sort)) {
                log.error("排序字段长度超过限制或包含sql关键字，请关注：{}", sort);
                return p;
            }

            boolean isAsc = false;
            if (!CharSequenceUtil.isBlank(order)) {
                if ("desc".equals(order.toLowerCase())) {
                    isAsc = false;
                } else if ("asc".equals(order.toLowerCase())) {
                    isAsc = true;
                }
            }

            if (isAsc) {
                p.addOrder(OrderItem.asc(sort));
            } else {
                p.addOrder(OrderItem.desc(sort));
            }

        }
        return p;
    }

    private void orderByHandler() {

    }

    /**
     * 生成条件搜索 全对象对比 equals
     * 如果需要like 需要另行处理
     *
     * @param object 对象（根据对象构建查询条件）
     * @return 查询wrapper
     */
    public static <T> QueryWrapper<T> initWrapper(Object object) {
        return initWrapper(object, null);
    }

    /**
     * 生成条件搜索 全对象对比
     *
     * @param object   对象
     * @param searchVo 查询条件
     * @return 查询wrapper
     */
    public static <T> QueryWrapper<T> initWrapper(Object object, SearchVO searchVo) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        //创建时间区间判定
        if (searchVo != null && CharSequenceUtil.isNotBlank(searchVo.getStartDate()) && CharSequenceUtil.isNotBlank(searchVo.getEndDate())) {
            Date start = DateUtil.parse(searchVo.getStartDate());
            Date end = DateUtil.parse(searchVo.getEndDate());
            queryWrapper.between("create_time", start, DateUtil.endOfDay(end));
        }
        if (object != null) {
            String[] fieldNames = BeanUtils.getFiledName(object);
            //遍历所有属性
            for (int j = 0; j < fieldNames.length; j++) {
                //获取属性的名字
                String key = fieldNames[j];
                //获取值
                Object value = BeanUtils.getFieldValueByName(key, object);
                //如果值不为空才做查询处理
                if (value != null && !"".equals(value)) {
                    //字段数据库中，驼峰转下划线
                    queryWrapper.eq(StringUtils.camel2Underline(key), value);
                }
            }
        }
        return queryWrapper;
    }


    /**
     * List 手动分页
     *
     * @param page 分页对象
     * @param list 分页集合
     * @return 范型结果
     */
    public static <T> List<T> listToPage(PageVO page, List<T> list) {

        int pageNumber = page.getPageNumber() - 1;
        int pageSize = page.getPageSize();

        if (pageNumber < 0) {
            pageNumber = 0;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        if (pageSize > 100) {
            pageSize = 100;
        }

        int fromIndex = pageNumber * pageSize;
        int toIndex = pageNumber * pageSize + pageSize;

        if (fromIndex > list.size()) {
            return new ArrayList<>();
        } else if (toIndex >= list.size()) {
            return list.subList(fromIndex, list.size());
        } else {
            return list.subList(fromIndex, toIndex);
        }
    }

    /**
     * 转换分页类型
     *
     * @param originPage 原分页
     * @param records    新分页数据
     * @param <T>        新类型
     * @return 新类型分页
     */
    public static <T> IPage<T> convertPage(IPage originPage, List<T> records) {
        IPage<T> resultPage = new Page<>();
        if (originPage != null) {
            resultPage.setCurrent(originPage.getCurrent());
            resultPage.setPages(originPage.getPages());
            resultPage.setTotal(originPage.getTotal());
            resultPage.setSize(originPage.getSize());
            resultPage.setRecords(records);
        }
        return resultPage;
    }
}
