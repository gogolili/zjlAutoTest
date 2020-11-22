package com.baiwang.moirai.service;

import com.baiwang.cloud.spush.client.SPushClient;
import com.baiwang.cloud.spush.client.constant.ClientConstant;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.model.user.MoiraiUserCondition;
import com.baiwang.spush.api.message.*;
import com.baiwang.spush.api.model.consumer.email.vo.EmailDetail;
import com.baiwang.spush.api.model.consumer.msg.vo.MsgDetail;
import com.baiwang.spush.api.model.spush.DefaultPushMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @Auther: liuzhenyun
 * @Date: 2019-06-20 11:24
 * @Description:
 */
//@ConditionalOnProperty(name = "use.method", havingValue = "true")
@Component
public class PushService {

    private static final Logger logger = LoggerFactory.getLogger(PushService.class);

    @Autowired(required = false)
    private SPushClient client;

    @Value("${spush.url:}")
    private String spushUrl;

    @Value("${spush.client.model:cloud}")
    private String SPUSH_CLIENT_MODEL;


    /*public void send(String data,String code){
        DefaultPushMessage pushMessage = new DefaultPushMessage();
        logger.info("推送消息：【{}】", data);
        pushMessage.setData(data);
        PushContext context = PushContext.build(new PushMsg().build(MsgType.INTERFACE, pushMessage, UUID.randomUUID().toString()));
        Map<String, List<String>> toUsers = new HashMap<>();
        List<String> tenanxList = new ArrayList<>();

        tenanxList.add("clickpaas");
        toUsers.put(ClientConstant.SPUSH_CLIENT_INTERFACE_IDENTIFER_TYPE_APPKEY, tenanxList);
        context.setToUsers(toUsers);
        context.setCode(code);

        Map<String,Object> extContext = new HashMap<>();
        extContext.put("SPUSH_CLIENT_MODEL","cloud");//本地、云端  local、cloud
        context.setShardingKey("baiwang_888");
        context.setExt(extContext);
        client.setRequestURL(spushUrl);
        client.push(context, new PushCallback(){
            @Override
            public void onSuccess(PushResult result) {
                logger.info("推送成功！，结果：【{}】",result);
            }
            @Override
            public void onFailure(PushResult result) {
                logger.info("推送失败！，结果：【{}】",result);
            }
        });
    }*/

    /**
     * 接口推送消息
     *
     * @param data       消息内容
     * @param constant   消息类型：1、租户，2、渠道，3、税号（销方），4、appkey，5、税号（购方）
     * @param code       业务代码
     * @param tenanxList 收件人
     */
    public void sendInterface(String data, String constant, String code, List<String> tenanxList) {
        DefaultPushMessage pushMessage = new DefaultPushMessage();
        logger.info("推送消息：【{}】", data);
        pushMessage.setData(data);
        PushContext context = PushContext.build(new PushMsg().build(MsgType.INTERFACE, pushMessage, UUID.randomUUID().toString()));
        Map<String, List<String>> toUsers = new HashMap<>();

        toUsers.put(constant, tenanxList);
        context.setToUsers(toUsers);
        context.setCode(code);

        Map<String, Object> extContext = new HashMap<>();
        extContext.put("SPUSH_CLIENT_MODEL", SPUSH_CLIENT_MODEL);//本地、云端  local、cloud
        context.setShardingKey("baiwang_888");
        context.setExt(extContext);
        client.setRequestURL(spushUrl);
        client.push(context, new PushCallback() {
            @Override
            public void onSuccess(PushResult result) {
                logger.info("接口推送成功！，结果：【{}】", result);
            }

            @Override
            public void onFailure(PushResult result) {
                logger.info("接口推送失败！，结果：【{}】", result);
            }
        });
    }

    /**
     * 发送短信验证码
     *
     * @param telephone  手机号
     * @param smsContent 验证码
     */
    public void sendMsg(String telephone, String smsContent) {
        MsgDetail msgDetail = new MsgDetail();
        msgDetail.setMobiles(telephone);
        msgDetail.setTenantId("2");
        msgDetail.setTemplate("BWY_MSG_YZM");
        Map<String, Object> map = new HashMap<>();
        map.put("yzm", smsContent);
        msgDetail.setTemplateParams(map);
        PushContext context = PushContext.build(new PushMsg().build(MsgType.SMG, msgDetail, UUID.randomUUID().toString()));

        Map<String, Object> extContext = new HashMap<>();
        extContext.put("SPUSH_CLIENT_MODEL", SPUSH_CLIENT_MODEL);
        context.setShardingKey("baiwang_001");
        context.setExt(extContext);
        client.setRequestURL(spushUrl);
        client.push(context, new PushCallback() {
            @Override
            public void onSuccess(PushResult result) {
                logger.info("短信推送成功！，结果：【{}】", result);
            }

            @Override
            public void onFailure(PushResult result) {
                logger.info("短信推送失败！，结果：【{}】", result);
            }
        });
    }

    /**
     * 发送邮件
     *
     * @param emailDetail 邮件信息
     */
    public void sendEmail(EmailDetail emailDetail) {
        PushContext context = PushContext.build(new PushMsg().build(MsgType.EMAIL, emailDetail, UUID.randomUUID().toString()));

        Map<String, Object> extContext = new HashMap<>();
        extContext.put("SPUSH_CLIENT_MODEL", SPUSH_CLIENT_MODEL);
        context.setShardingKey("baiwang_001");
        context.setExt(extContext);
        client.setRequestURL(spushUrl);
        client.push(context, new PushCallback() {
            @Override
            public void onSuccess(PushResult result) {
                logger.info("邮件推送成功！，结果：【{}】", result);
            }

            @Override
            public void onFailure(PushResult result) {
                logger.info("邮件推送失败！，结果：【{}】", result);
            }
        });
    }
}
