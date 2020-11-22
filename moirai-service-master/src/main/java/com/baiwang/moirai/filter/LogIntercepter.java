package com.baiwang.moirai.filter;

import com.baiwang.cloud.common.enumutil.ErrorType;
import com.baiwang.cloud.common.model.ErrorMessage;
import com.baiwang.moirai.common.Constants;
import com.baiwang.moirai.common.WebContext;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.service.MoiraiOrgService;
import com.baiwang.moirai.utils.HttpInfoUtils;
import com.baiwang.moirai.utils.JacksonUtil;
import com.baiwang.moirai.utils.StrUtils;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class LogIntercepter implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(LogIntercepter.class);

    @Autowired
    @Qualifier("redisTrafficTemplate")
    private RedisTemplate redisTemplate;

    /**
     * 控制器某个方法执行前被调用 boolean:是否调用控制器中的方法
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String url = request.getRequestURL().toString();
        String requestURI = request.getRequestURI();
        String bwToken = HttpInfoUtils.getToken(Constants.BWTOKEN, request);
        String remoteAddr = request.getRemoteAddr();
        int port = request.getRemotePort();
        logger.info("LogIntercepter start: url={}, remoteAddr={}, remotePort={}", url, remoteAddr, port);
        HttpSession session = request.getSession(true);
        Object user = session.getAttribute("moiraiUser");
        Object org = session.getAttribute("moiraiOrg");
        if (user == null || org == null) {
            if (!StrUtils.isEmpty(bwToken)) {
                try {
                    Object token = redisTemplate.opsForValue().get(Constants.REDIS_ACCESSTOKEN + bwToken);
                    if (token != null) {
                        HashMap hashMap = JacksonUtil.jsonStrToObject(token.toString(), HashMap.class);
                        Object o = hashMap.get("userId");
                        Object id = hashMap.get("orgId");
                        if (o != null && id != null) {
                            Long userId = Long.valueOf(o.toString());
                            Object userToken = redisTemplate.opsForValue().get(Constants.REDIS_USER + userId);
                            if (userToken != null) {
                                MoiraiUser moiraiUser = JacksonUtil.jsonStrToObject(userToken.toString(), MoiraiUser.class);
                                Long orgId = Long.valueOf(id.toString());
                                MoiraiOrg pOrg = new MoiraiOrg();
                                pOrg.setOrgId(orgId);
                                MoiraiOrgService moiraiOrgService = WebContext.getBean(MoiraiOrgService.class);
                                MoiraiOrg moiraiOrg = moiraiOrgService.selectOneOrg(pOrg);
                                HttpSession httpSession = request.getSession(true);
                                httpSession.setAttribute("moiraiUser", moiraiUser);
                                httpSession.setAttribute("moiraiOrg", moiraiOrg);
                            }
                        }
                    }
                } catch (Exception e) {
                    //标准错误日志格式-禁止自定义错误格式
                    MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_TENANT_ERROR;
                    logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);
                }
                logger.info("session不存在，重新生成,请求token:{}", bwToken);
            }
        }
        return true;
    }

    /**
     * 控制器中方法执行完之后调用该方法,如果有异常则放弃调用该方法
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
        ModelAndView modelAndView) {
    }

    /**
     * 控制器中方法执行后,任何时候都会调用该方法
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
        throws Exception {
    }
}
