package com.baiwang.moirai.utils;

import com.baiwang.moirai.exception.MoiraiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

/**
 * MoiraiUtils工具类
 */
public class MoiraiUtils {

    private static final Logger logger = LoggerFactory.getLogger(MoiraiUtils.class);

    public MoiraiUtils() {
    }

    /**
     * 判断不为空
     */
    public static boolean isNotNullOrNotEmpty(Object obj) {
        return !isNullOrEmpty(obj);
    }

    /**
     * 判断为空
     */
    public static boolean isNullOrEmpty(Object obj) {
        if (obj == null) {
            return true;
        } else if (obj instanceof CharSequence) {
            return ((CharSequence) obj).length() == 0;
        } else if (obj instanceof Collection) {
            return ((Collection) obj).isEmpty();
        } else if (obj instanceof Map) {
            return ((Map) obj).isEmpty();
        } else if (!(obj instanceof Object[])) {
            return false;
        } else {
            Object[] object = (Object[]) ((Object[]) obj);
            if (object.length == 0) {
                return true;
            } else {
                boolean empty = true;

                for (int i = 0; i < object.length; ++i) {
                    if (!isNullOrEmpty(object[i])) {
                        empty = false;
                        break;
                    }
                }

                return empty;
            }
        }
    }

    /**
     * 为空抛出错误
     *
     * @param object
     * @param e
     */
    public static void nullOrEmptyThrow(Object object, RuntimeException e) {
        if (isNullOrEmpty(object)) {
            logger.error(e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 不为空 抛出错误
     *
     * @param object
     * @param e
     */
    public static void notNullOrNotEmptyThrow(Object object, RuntimeException e) {
        if (isNotNullOrNotEmpty(object)) {
            logger.error(e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 判断是否相等
     * 适用于基本类型和字符串
     *
     * @param o1
     * @param o2
     * @return
     */
    public static boolean equals(Object o1, Object o2) {
        if (o1 == null && o2 == null)
            return true;
        if (o1 == null || o2 == null)
            return false;
        if (isBaseType(o1) && isBaseType(o2))
            return o1.equals(o2);
        if (o1 instanceof String && o2 instanceof String)
            return o1.equals(o2);
        throw new MoiraiException("-1", "只能比较基本类型或者字符串类型");
    }

    /**
     * 不相等 抛出错误
     */
    public static void unequalThrow(Object o1, Object o2, RuntimeException e) {
        if (!equals(o1, o2)) {
            logger.error(e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 相等 抛出错误
     */
    public static void equalThrow(Object o1, Object o2, RuntimeException e) {
        if (equals(o1, o2)) {
            logger.error(e.getMessage(), e);
            throw e;
        }
    }

    public static boolean isBaseType(Object object) {
        Class className = object.getClass();
        if (className.equals(Integer.class) ||
                className.equals(Byte.class) ||
                className.equals(Long.class) ||
                className.equals(Double.class) ||
                className.equals(Float.class) ||
                className.equals(Character.class) ||
                className.equals(Short.class) ||
                className.equals(Boolean.class)) {
            return true;
        }
        return false;
    }
}
