package com.baiwang.moirai.event;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baiwang.cloud.spush.client.constant.ClientConstant;
import com.baiwang.moirai.mapper.MoiraiChannelTenantMapper;
import com.baiwang.moirai.mapper.MoiraiTenantMapper;
import com.baiwang.moirai.mapper.MoiraiUserMapper;
import com.baiwang.moirai.model.tenant.MoiraiChannelTenant;
import com.baiwang.moirai.model.tenant.MoiraiTenant;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.service.PushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: liuzhenyun
 * @Date: 2019-06-19 17:35
 * @Description:
 */
@ConditionalOnProperty(name = "use.method", havingValue = "true")
@Component
@EnableAsync
public class UserEventListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired(required = false)
    private PushService pushService;

    @Autowired
    private MoiraiUserMapper moiraiUserMapper;

    @Autowired
    private MoiraiTenantMapper moiraiTenantMapper;

    @Autowired
    private MoiraiChannelTenantMapper moiraiChannelTenantMapper;

    @Value("${clickpaas_qdbm:0}")
    private Long qdbm;

    /**
     * 处理用户添加/修改
     *
     * @param event
     */
    @Async
    @EventListener(condition = "#event.modify")
    public void dealModify(UserEvent event) {
        MoiraiUser user = event.getUser();
        logger.info("监听到用户添加/修改的消息：{}", user);
        try {
            Thread.currentThread().sleep(1000);
        } catch (InterruptedException e) {
            logger.error("休眠失败！");
        }
        MoiraiTenant tenant = moiraiTenantMapper.selectByPrimaryKey(user.getTenantId());
        if (tenant == null){
            logger.error("根据租户id未获取到租户信息！租户id：{}", user.getTenantId());
            return;
        }
        MoiraiChannelTenant query = new MoiraiChannelTenant();
        query.setTenantId(tenant.getTenantId());
        List<MoiraiChannelTenant> channelTenantList = moiraiChannelTenantMapper.queryList(query);
        for (int i = 0; i < channelTenantList.size(); i++){
            if (qdbm.equals(channelTenantList.get(i).getQdBm())) {
                //开通了，同步信息
                logger.info("用户所在租户开通了CP功能，开始同步用户信息");
                JSONObject json = new JSONObject();
                json.put("externalId", user.getUserId());
                json.put("name", user.getUserName());
                json.put("externalDepartmentId", user.getOrgId());
                json.put("externalTenantId", user.getTenantId());
                json.put("username", user.getUserAccount());
                send(json.toJSONString(), "1104");
            }
        }
    }

    /**
     * 处理用户删除
     *
     * @param event
     */
    @Async
    @EventListener(condition = "#event.delete")
    public void dealDelete(UserEvent event) {
        MoiraiUser user = event.getUser();
        logger.info("监听到用户删除的消息：{}", user);
//        MoiraiTenant tenant = moiraiTenantMapper.selectByPrimaryKey(user.getTenantId());
        MoiraiChannelTenant query = new MoiraiChannelTenant();
        query.setTenantId(user.getTenantId());
        List<MoiraiChannelTenant> channelTenantList = moiraiChannelTenantMapper.queryList(query);
        for (int i = 0; i < channelTenantList.size(); i++) {
            if (qdbm.equals(channelTenantList.get(i).getQdBm())) {
                //开通了，同步信息
                logger.info("用户所在租户开通了CP功能，开始同步用户信息");
                JSONObject json = new JSONObject();
                json.put("externalId", user.getUserId());
                json.put("externalTenantId", user.getTenantId());
                send(json.toJSONString(), "1105");
            }
        }
    }

    /**
     * 批量同步用户
     *
     * @param event
     */
    @Async
    @EventListener(condition = "#event.batchSync")
    public void dealSync(UserEvent event) {

        Long tenantId = event.getTenantId();
        MoiraiTenant tenant = moiraiTenantMapper.selectByPrimaryKey(tenantId);
        MoiraiChannelTenant query = new MoiraiChannelTenant();
        query.setTenantId(tenant.getTenantId());
        List<MoiraiChannelTenant> channelTenantList = moiraiChannelTenantMapper.queryList(query);
        for (int i = 0; i < channelTenantList.size(); i++) {
            if (qdbm.equals(channelTenantList.get(i).getQdBm())) {
                List<MoiraiUser> userList = moiraiUserMapper.selectByTenantId(tenantId);
                logger.info("监听到批量同步用户的消息，用户数量：{}", userList.size());
                if (userList.isEmpty()) {
                    logger.info("批量同步用户数量为0，不做操作");
                } else {
                    JSONArray users = new JSONArray();
                    JSONObject json;
                    for (MoiraiUser user : userList) {
                        json = new JSONObject();
                        json.put("externalId", user.getUserId());
                        json.put("name", user.getUserName());
                        json.put("externalDepartmentId", user.getOrgId());
                        json.put("externalTenantId", user.getTenantId());
                        json.put("username", user.getUserAccount());
                        users.add(json);
                    }
                    send(users.toJSONString(), "1106");
                    logger.info("事件监听器中，发送批量同步用户消息完毕");
                }
            } else {
                logger.info("租户：【{}】未开通cp功能，不进行用户同步", tenantId);
            }
        }
    }

    private void send(String data, String code) {
        List<String> tenanxList = new ArrayList<>();
        tenanxList.add("clickpaas");
        pushService.sendInterface(data, ClientConstant.SPUSH_CLIENT_INTERFACE_IDENTIFER_TYPE_APPKEY, code, tenanxList);
    }
}
