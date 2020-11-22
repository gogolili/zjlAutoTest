package com.baiwang.moirai.event;

import com.alibaba.fastjson.JSONObject;
import com.baiwang.cloud.spush.client.constant.ClientConstant;
import com.baiwang.moirai.model.tenant.MoiraiTenantVO;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.service.PushService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

/**
 * @Author: liuzhenyun
 * @Date: 2019-11-29 14:38
 * @Description:
 */
@ConditionalOnProperty(name = "use.method", havingValue = "true")
@Component
@EnableAsync
public class TenantEventListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired(required = false)
    private PushService pushService;

    /**
     * 处理租户添加
     *
     * @param event
     */
    @Async
    @EventListener()
    public void dealModify(TenantEvent event) {
        MoiraiTenantVO tenant = event.getMoiraiTenant();
        MoiraiUser user = event.getMoiraiUser();

        logger.info("监听到租户添加的消息,租户：[{}]，用户：[{}]", tenant, user);
        List<String> tenanxList = new ArrayList<>();
        Set<Long> qdbmSet = new HashSet<>();
        if (tenant.getQdBm() != null) {
            qdbmSet.add(tenant.getQdBm());
        }
        if (StringUtils.isNotBlank(tenant.getQdBms())) {
            String[] qdbms = tenant.getQdBms().split(",");
            for (int i = 0; i < qdbms.length; i++) {
                qdbmSet.add(Long.valueOf(qdbms[i]));
            }
        }
        tenanxList.addAll(new ArrayList(qdbmSet));

        JSONObject msg=(JSONObject)JSONObject.toJSON(user);
        msg.put("tenantName",tenant.getTenantName());
        msg.put("taxCode",tenant.getTaxCode());

        if (tenanxList.size() > 0) {
            pushService.sendInterface(msg.toString(), ClientConstant.SPUSH_CLIENT_INTERFACE_IDENTIFER_TYPE_CHANNEL,"1201",tenanxList);
        }
    }
}
