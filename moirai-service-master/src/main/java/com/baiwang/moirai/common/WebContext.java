package com.baiwang.moirai.common;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * web端上下文,包含spring 容器
 *
 * @author qn
 * @revision 1.0
 * @see [相关类/方法]
 */
@Component
public class WebContext extends RequestContext implements ApplicationContextAware {
    private static ApplicationContext springContext;

    private WebContext() {
    }

    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        WebContext.springContext = applicationContext;
    }

    public static ApplicationContext getSpringContext() {
        return WebContext.springContext;
    }

    public static <T> T getBean(Class<T> clazz) {
        return springContext.getBean(clazz);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String beanName) {
        return (T) springContext.getBean(beanName);
    }

}
