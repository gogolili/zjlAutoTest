package com.baiwang.moirai.utils;

import com.baiwang.cloud.common.enumutil.ErrorType;
import com.baiwang.cloud.common.model.ErrorMessage;
import com.baiwang.moirai.common.WebContext;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

/**
 * @author sxl 2019-02-18
 */
public final class Base64Utils {
    private static final Logger logger = LoggerFactory.getLogger(Base64Utils.class);
    private static final String charset = "utf-8";

    /**
     * 解密
     *
     * @param data
     * @return
     */
    public static byte[] decode(String data) {
        if (null == data) {
            return null;
        }
        return Base64.getDecoder().decode(data.getBytes());
    }

    /**
     * 加密
     *
     * @param data
     * @return
     * @author sxl
     */
    public static String encode(byte[] data) {
        try {
            if (null == data) {
                return null;
            }
            return byteToString(Base64.getEncoder().encode(data));
        } catch (UnsupportedEncodingException e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_CHARACTOR_ENCRYT_ERROR;
            logger.error(new ErrorMessage(requestURI,errorEnum.getCode(),String.format("字符串：%s，加密异常", data), ErrorType.CustomerError).toString(),e);
        }
        return null;
    }

    /**
     * 加密
     *
     * @param data
     * @return
     * @author sxl
     */
    public static String encode(String data) {
        try {
            if (null == data) {
                return null;
            }
            return byteToString(Base64.getEncoder().encode(data.getBytes()));
        } catch (UnsupportedEncodingException e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_CHARACTOR_ENCRYT_ERROR;
            logger.error(new ErrorMessage(requestURI,errorEnum.getCode(),String.format("字符串：%s，加密异常", data), ErrorType.CustomerError).toString(),e);
        }
        return null;
    }

    /**
     * URL加密
     *
     * @param data
     * @return
     */
    public static String UrlEncode(String data) {
        try {
            if (null == data) {
                return null;
            }
            return byteToString(Base64.getUrlEncoder().encode(data.getBytes()));
        } catch (UnsupportedEncodingException e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_CHARACTOR_ENCRYT_ERROR;
            logger.error(new ErrorMessage(requestURI,errorEnum.getCode(),String.format("字符串：%s，加密异常", data), ErrorType.CustomerError).toString(),e);
        }
        return null;
    }

    /**
     * MILE加密
     *
     * @param data
     * @return
     */
    public static String MileEncode(String data) {
        try {
            if (null == data) {
                return null;
            }
            return byteToString(Base64.getMimeEncoder().encode(data.getBytes()));
        } catch (UnsupportedEncodingException e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_CHARACTOR_ENCRYT_ERROR;
            logger.error(new ErrorMessage(requestURI,errorEnum.getCode(),String.format("字符串：%s，加密异常", data), ErrorType.CustomerError).toString(),e);
        }
        return null;
    }

    /**
     * 解密
     *
     * @param data
     * @return
     * @author sxl
     */
    public static String decodeString(String data) {
        try {
            if (null == data) {
                return null;
            }
            return new String(Base64.getDecoder().decode(data.getBytes()), charset);
        } catch (UnsupportedEncodingException e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_CHARACTOR_DEENCRYT_ERROR;
            logger.error(new ErrorMessage(requestURI,errorEnum.getCode(),String.format("字符串：%s，加密异常", data), ErrorType.CustomerError).toString(),e);
        }
        return null;
    }

    /**
     * URL解密
     *
     * @param data
     * @return
     * @author sxl
     */
    public static String UrlDecodeString(String data) {
        try {
            if (null == data) {
                return null;
            }
            return new String(Base64.getUrlDecoder().decode(data.getBytes()), charset);
        } catch (UnsupportedEncodingException e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_CHARACTOR_DEENCRYT_ERROR;
            logger.error(new ErrorMessage(requestURI,errorEnum.getCode(),String.format("字符串：%s，加密异常", data), ErrorType.CustomerError).toString(),e);
        }
        return null;
    }

    /**
     * MILE解密
     *
     * @param data
     * @return
     * @author sxl
     */
    public static String MileDecodeString(String data) {
        try {
            if (null == data) {
                return null;
            }
            return new String(Base64.getMimeDecoder().decode(data.getBytes()), charset);
        } catch (UnsupportedEncodingException e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_CHARACTOR_DEENCRYT_ERROR;
            logger.error(new ErrorMessage(requestURI,errorEnum.getCode(),String.format("字符串：%s，加密异常", data), ErrorType.CustomerError).toString(),e);

        }
        return null;
    }


    /**
     * byte[]转String
     *
     * @param chars
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String byteToString(byte[] chars) throws UnsupportedEncodingException {
        return new String(chars, charset);
    }
}