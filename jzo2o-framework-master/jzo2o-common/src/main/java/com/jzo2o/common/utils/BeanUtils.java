package com.jzo2o.common.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.jzo2o.common.handler.ConvertHandler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 继承自 hutool 的BeanUtil，增加了bean转换时自定义转换器的功能
 *
 * @author itcast
 */
public class BeanUtils extends BeanUtil {

    /**
     * 将原对象转换成目标对象，对于字段不匹配的字段可以使用转换器处理
     *
     * @param source         原对象
     * @param clazz          目标对象的class
     * @param convertHandler 转换器
     * @param <O>            原对象类型
     * @param <T>            目标对象类型
     * @return 目标对象
     */
    public static <O, T> T copyBean(O source, Class<T> clazz, ConvertHandler<O, T> convertHandler) {
        T target = copyBean(source, clazz);
        if (convertHandler != null) {
            convertHandler.map(source, target);
        }
        return target;
    }

    /**
     * 复制set集合中的属性到目标对象中
     *
     * @param originSet 原始对象集合
     * @param targetType 目标对象类型
     * @param convertHandler 目标对象数据转换器
     * @return
     * @param <O> 原始对象类型
     * @param <T> 目标对象类型
     */
    public static <O, T> List<T> copyToList(Set<O> originSet, Class<T> targetType, ConvertHandler<O, T> convertHandler) {
        if (CollUtils.isEmpty(originSet)) {
            return null;
        }
        return originSet.stream().map(o -> copyBean(o, targetType, convertHandler)).collect(Collectors.toList());
    }

    /**
     * 复制set集合中的属性到目标对象中
     *
     * @param originSet 原始对象集合
     * @param targetType 目标对象类型
     * @return
     * @param <O> 原始对象类型
     * @param <T> 目标对象类型
     */
    public static <O, T> List<T> copyToList(Set<O> originSet, Class<T> targetType) {
        return copyToList(originSet, targetType, (ConvertHandler<O, T>) null);
    }

    /**
     * 复制集合中的Bean属性
     *
     * @param originList     原Bean集合
     * @param targetType     目标Bean类型
     * @param convertHandler 特殊对象类型转换器，可传null，即不进行特殊处理
     * @return 复制后的List
     */
    public static <O, T> List<T> copyToList(List<O> originList, Class<T> targetType, ConvertHandler<O, T> convertHandler) {
        List<T> targetList = cn.hutool.core.bean.BeanUtil.copyToList(originList, targetType);
        //特殊类型转换
        if (CollUtil.isNotEmpty(targetList) && ObjectUtil.isNotEmpty(convertHandler)) {
            for (int i = 0; i < originList.size(); i++) {
                convertHandler.map(originList.get(i), targetList.get(i));
            }
        }
        return targetList;
    }

    /**
     * 将原对象转换成目标对象，对于字段不匹配的字段可以使用转换器处理
     *
     * @param source 原对象
     * @param clazz  目标对象的class
     * @param <R>    原对象类型
     * @param <T>    目标对象类型
     * @return 目标对象
     */
    public static <R, T> T copyBean(R source, Class<T> clazz) {
        if (source == null) {
            return null;
        }
        return toBean(source, clazz);
    }

    /**
     * 将列表转换另一种类型的列表
     *
     * @param originList  原列表
     * @param targetClass 目标类型class
     * @param <R>         原列表元素类型
     * @param <T>         目标列表元素类型
     * @return 目标列表
     */
    @Deprecated
    public static <R, T> List<T> copyList(List<R> originList, Class<T> targetClass) {
        if (CollUtils.isEmpty(originList)) {
            return CollUtils.emptyList();
        }
        return copyToList(originList, targetClass);
    }

    @Deprecated
    public static <O, T> List<T> copyList(List<O> list, Class<T> clazz, ConvertHandler<O, T> convertHandler) {
        if (list == null || list.size() == 0) {
            return CollUtils.emptyList();
        }
        return list.stream().map(r -> copyBean(r, clazz, convertHandler)).collect(Collectors.toList());
    }

    public static <T> T copyIgnoreNull(T source,T target, Class<T> clazz){
        //1.源数据和目标数据都转为map
        Map<String, Object> oldData = BeanUtil.beanToMap(target,false,true);
        Map<String, Object> newData = BeanUtil.beanToMap(source,false,true);

        //2.用新数据覆盖旧数据
        oldData.putAll(newData);

        //3.map转为bean返回
        return BeanUtil.mapToBean(oldData, clazz, false, new CopyOptions());
    }

    /**
     * 复制属性
     *
     * @param objectFrom 源自对象
     * @param objectTo   复制给对象
     */
    public static void copyProperties(Object objectFrom, Object objectTo) {
        BeanUtils.copyProperties(objectFrom, objectTo);
    }


    /**
     * 获取属性名数组
     *
     * @param o 获取字段的对象
     * @return 返回各个字段
     */
    public static String[] getFiledName(Object o) {
        Field[] fields = o.getClass().getDeclaredFields();
        Field[] superFields = o.getClass().getSuperclass().getDeclaredFields();
        String[] fieldNames = new String[fields.length + superFields.length];
        int index = 0;
        for (int i = 0; i < fields.length; i++) {
            fieldNames[index] = fields[i].getName();
            index++;
        }
        for (int i = 0; i < superFields.length; i++) {
            if ("id".equals(superFields[i].getName())) {
                continue;
            }
            fieldNames[index] = superFields[i].getName();
            index++;
        }
        return fieldNames;
    }

    /**
     * 根据属性名获取属性值
     *
     * @param fieldName 属性名
     * @param o         对象
     * @return 属性值
     */
    public static Object getFieldValueByName(String fieldName, Object o) {
        try {
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String getter = "get" + firstLetter + fieldName.substring(1);
            Method method = o.getClass().getMethod(getter, new Class[]{});
            Object value = method.invoke(o, new Object[]{});
            return value;
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 将对象转换为key value
     * A=a&B=b&C=c 格式
     *
     * @param object 对象
     * @return 格式化结果
     */
    public static String formatKeyValuePair(Object object) {
        //准备接受的字符串
        StringBuilder stringBuffer = new StringBuilder();
        //获取对象字段
        String[] fieldNames = BeanUtils.getFiledName(object);
        //遍历所有属性
        for (int j = 0; j < fieldNames.length; j++) {
            //不是第一个并且不是最后一个，拼接&
            if (j != 0) {
                stringBuffer.append("&");
            }
            //获取属性的名字
            String key = fieldNames[j];
            //获取值
            Object value = BeanUtils.getFieldValueByName(key, object);
            assert value != null;
            stringBuffer.append(key).append("=").append(value.toString());
        }
        return stringBuffer.toString();
    }

    /**
     * key value键值对 转换为 对象
     * A=a&B=b&C=c 格式 转换为对象
     *
     * @param str 对象字符串
     * @param t   范型
     * @param <T> 范型
     * @return 格式化结果
     */
    public static <T> T formatKeyValuePair(String str, T t) {
        //填写对参数键值对
        String[] params = str.split("&");

        //获取对象字段
        String[] fieldNames = BeanUtils.getFiledName(t);

        try {
            //循环每个参数
            for (String param : params) {
                String[] keyValues = param.split("=");
                for (int i = 0; i < fieldNames.length; i++) {
                    if (fieldNames[i].equals(keyValues[0])) {
                        Field f = t.getClass().getDeclaredField(fieldNames[i]);
                        f.setAccessible(true);
                        //长度为2 才转换，否则不转
                        if (keyValues.length == 2) {
                            f.set(t, keyValues[1]);
                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }
}