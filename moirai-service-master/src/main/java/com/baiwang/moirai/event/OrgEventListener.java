package com.baiwang.moirai.event;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baiwang.cloud.sdk.SendEventUtils;
import com.baiwang.cloud.sdk.model.EventModle;
import com.baiwang.cloud.spush.client.constant.ClientConstant;
import com.baiwang.moirai.mapper.MoiraiChannelTenantMapper;
import com.baiwang.moirai.mapper.MoiraiOrgMapper;
import com.baiwang.moirai.mapper.MoiraiTenantMapper;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.tenant.MoiraiChannelTenant;
import com.baiwang.moirai.model.tenant.MoiraiTenant;
import com.baiwang.moirai.service.PushService;
import org.apache.commons.lang3.StringUtils;
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
 * @Date: 2019-06-20 11:16
 * @Description:
 */
@ConditionalOnProperty(name = "use.method", havingValue = "true")
@Component
@EnableAsync
public class OrgEventListener {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired(required = false)
    private PushService pushService;

    @Autowired
    private MoiraiOrgMapper moiraiOrgMapper;

    @Autowired
    private MoiraiTenantMapper moiraiTenantMapper;

    @Autowired
    private MoiraiChannelTenantMapper moiraiChannelTenantMapper;

    @Autowired
    private SendEventUtils sendEventUtils;

    @Value("${clickpaas_qdbm:0}")
    private Long qdbm;

    /**
     * 处理机构添加/修改
     *
     * @param event
     */
    @Async
    @EventListener(condition = "#event.modify")
    public void dealModify(OrgEvent event) {
        MoiraiOrg org = event.getOrg();
        logger.info("监听到机构添加/修改的消息：{}", org);

        MoiraiChannelTenant query = new MoiraiChannelTenant();
        query.setTenantId(org.getTenantId());
        List<MoiraiChannelTenant> channelTenantList = moiraiChannelTenantMapper.queryList(query);
        if (channelTenantList.isEmpty()) {
            try {
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException e) {
                logger.error("休眠失败！");
            }
            channelTenantList = moiraiChannelTenantMapper.queryList(query);
        }
        for (int i = 0; i < channelTenantList.size(); i++) {
            if (qdbm.equals(channelTenantList.get(i).getQdBm())) {
                //开通了，同步信息
                logger.info("机构开通了CP功能，开始同步机构信息");
                JSONObject json = new JSONObject();
                json.put("externalPid", org.getParentOrg());
                json.put("externalId", org.getOrgId());
                json.put("externalTenantId", org.getTenantId());
                json.put("name", org.getOrgName());
                json.put("orgCode", org.getOrgCode());
                json.put("taxCode", org.getTaxCode());
                json.put("orgType", org.getOrgType());
                send(json.toJSONString(), "1101");
            }
        }
        if (StringUtils.isNotBlank(org.getTaxCode())) {
            EventModle eventDBModle = new EventModle();
            eventDBModle.setEventId(org.getTaxCode());
            eventDBModle.setEventType("USER_REGIST");//常量值
            JSONObject data = new JSONObject();
            data.put("taxNo", org.getTaxCode());
            eventDBModle.setEventData(data.toJSONString());
            List<EventModle> eventDBModles = new ArrayList<>();
            eventDBModles.add(eventDBModle);
            sendEventUtils.sendEvent(eventDBModles);
        }
    }

    /**
     * 处理机构删除
     *
     * @param event
     */
    @Async
    @EventListener(condition = "#event.delete")
    public void dealDelete(OrgEvent event) {
        MoiraiOrg org = event.getOrg();
        logger.info("监听到机构删除的消息：{}", org);
        MoiraiTenant tenant = moiraiTenantMapper.selectByPrimaryKey(org.getTenantId());
        MoiraiChannelTenant query = new MoiraiChannelTenant();
        query.setTenantId(tenant.getTenantId());
        List<MoiraiChannelTenant> channelTenantList = moiraiChannelTenantMapper.queryList(query);
        for (int i = 0; i < channelTenantList.size(); i++) {
            if (qdbm.equals(channelTenantList.get(i).getQdBm())) {
                //开通了，同步信息
                logger.info("机构开通了CP功能，开始同步机构信息");
                JSONObject json = new JSONObject();
                json.put("externalId", org.getOrgId());
                json.put("externalTenantId", org.getTenantId());
                send(json.toJSONString(), "1102");
            }
        }
    }

    /**
     * 批量同步机构
     *
     * @param event
     */
    @Async
    @EventListener(condition = "#event.batchSync")
    public void dealSync(OrgEvent event) {

        MoiraiTenant tenant = moiraiTenantMapper.selectByPrimaryKey(event.getTenantId());
        MoiraiChannelTenant query = new MoiraiChannelTenant();
        query.setTenantId(tenant.getTenantId());
        List<MoiraiChannelTenant> channelTenantList = moiraiChannelTenantMapper.queryList(query);
        List<MoiraiOrg> orgList = moiraiOrgMapper.queryOrgTreeByTenant(event.getTenantId());
        for (int i = 0; i < channelTenantList.size(); i++) {
            if (qdbm.equals(channelTenantList.get(i).getQdBm())) {
                logger.info("监听到批量同步机构的消息,机构数量：{}", orgList.size());
                if (orgList.isEmpty()) {
                    logger.info("批量同步机构数量为0，不做操作");
                } else {
                    JSONArray orgs = new JSONArray();
                    JSONObject json;
                    for (MoiraiOrg org : orgList) {
                        json = new JSONObject();
                        json.put("externalPid", org.getParentOrg());
                        json.put("externalId", org.getOrgId());
                        json.put("externalTenantId", org.getTenantId());
                        json.put("name", org.getOrgName());
                        json.put("orgCode", org.getOrgCode());
                        json.put("taxCode", org.getTaxCode());
                        json.put("orgType", org.getOrgType());
                        orgs.add(json);
                    }
                    send(orgs.toJSONString(), "1103");
                    logger.info("事件监听器中，发送批量同步机构消息完毕");
                }
            }
        }
        List<EventModle> eventDBModles = new ArrayList<>();
        for (MoiraiOrg org : orgList) {
            if (StringUtils.isNotBlank(org.getTaxCode())) {
                EventModle eventDBModle = new EventModle();
                eventDBModle.setEventId(org.getTaxCode());
                eventDBModle.setEventType("USER_REGIST");//常量值
                JSONObject data = new JSONObject();
                data.put("taxNo", org.getTaxCode());
                eventDBModle.setEventData(data.toJSONString());
                eventDBModles.add(eventDBModle);
            }
        }
        if (!eventDBModles.isEmpty()){
            sendEventUtils.sendEvent(eventDBModles);
        }
    }

    private void send(String data, String code) {
        List<String> tenanxList = new ArrayList<>();
        tenanxList.add("clickpaas");
        pushService.sendInterface(data, ClientConstant.SPUSH_CLIENT_INTERFACE_IDENTIFER_TYPE_APPKEY, code, tenanxList);
    }
}
