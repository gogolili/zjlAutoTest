package com.baiwang.moirai.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * request请求上下文
 *
 * @author qn
 * @revision 1.0
 * @see [相关类/方法]
 */
public abstract class RequestContext {
    public static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes()).getRequest();
    }

    public static HttpServletResponse getResponse() {
        return ((ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes()).getResponse();
    }

    @SuppressWarnings("unchecked")
    public static <T> T getRequestAttribute(String name) {
        return (T) getRequest().getAttribute(name);
    }

    public static void setRequestAttribute(String name, Object value) {
        getRequest().setAttribute(name, value);
    }

    public static String getRequestParameter(String name) {
        return getRequest().getParameter(name);
    }

    /**
     * 获取HTTPSession对象
     *
     */
    public static HttpSession getSession() {
        return getRequest().getSession(true);
    }
}
