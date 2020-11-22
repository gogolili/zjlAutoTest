package com.baiwang.moirai.service;

import com.baiwang.cloud.common.enumutil.ErrorType;
import com.baiwang.cloud.common.model.ErrorMessage;
import com.baiwang.moirai.common.Constants;
import com.baiwang.moirai.common.WebContext;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.spush.api.model.consumer.email.vo.EmailDetail;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.commons.beanutils.LazyDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * All rights Reserved, Designed By www.baiwang.com
 *
 * @version v1.0
 */
@Component
public class PasswordService {
    private static final Logger logger = LoggerFactory.getLogger(PasswordService.class);

    @Value("${user.random.password:true}")
    private boolean randomPassword;

    @Autowired(required = false)
    private PushService pushService;

    /**
     * @return
     */
    public boolean calculatePasswordMark() {
        return randomPassword;
    }

    /**
     * @return
     */
    public void sendMail(String version, String subject, String template, String title,
        List<LazyDynaBean> lazyDynaBeans) {
        if (randomPassword && Constants.MOIRAI_VERSION_V2.equals(version)) {
            for (LazyDynaBean lazyDynaBean : lazyDynaBeans) {
                Object userName = lazyDynaBean.get("userName");
                Object context = lazyDynaBean.get("context");
                List<String> emails = (List) lazyDynaBean.get("emails");

                Map<String, Object> templateParams = new HashMap<>();
                templateParams.put("userName", userName);

                LocalDate localDate = LocalDate.now();
                DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                templateParams.put("date", localDate.format(formatterDate));
                LocalTime localTime = LocalTime.now();
                DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("HH:mm:ss");
                templateParams.put("time", localTime.format(formatterTime));
                templateParams.put("context", context);
                templateParams.put("title", title);

                EmailDetail emailDetail = new EmailDetail();
                emailDetail.setSubject(subject);
                emailDetail.setHtml(true);
                emailDetail.setTemplate(template);
                emailDetail.setTemplateParams(templateParams);
                emailDetail.setToUsers(emails);

                try {
                    pushService.sendEmail(emailDetail);
                } catch (Exception e) {
                    String requestURI = WebContext.getRequest().getRequestURI();
                    MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_SEND_EMAIL_FAIL;
                    logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg() + ",邮箱为:" + emails.toString(), ErrorType.CustomerError).toString(), e);
                }
            }
        }
    }

    /**
     * @return
     */
    public String calculatePassword(String version) {
        String password;
        if (randomPassword && Constants.MOIRAI_VERSION_V2.equals(version)) {
            password = buildRandomPassword();
        } else {
            password = Constants.INIT_PWD;
        }
        return password;
    }

    /**
     * @return
     */
    private String buildRandomPassword() {
        int length = 8;
        // 最终生成的密码
        String password = "";
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            // 随机生成0或1，用来确定是当前使用数字还是字母 (0则输出数字，1则输出字母)
            int charOrNum = random.nextInt(2);
            if (charOrNum == 1) {
                // 随机生成0或1，用来判断是大写字母还是小写字母 (0则输出小写字母，1则输出大写字母)
                int temp = random.nextInt(2) == 1 ? 65 : 97;
                password += (char) (random.nextInt(26) + temp);
            } else {
                // 生成随机数字
                password += random.nextInt(10);
            }
        }
        return password;
    }

    public void sendTenantMail(String version, String subject, String template, String title,
        Map<String, List<LazyDynaBean>> map) {
        List<LazyDynaBean> mailBean = new ArrayList<>();
        for (String bean : map.keySet()) {
            StringBuilder builder = contentHtmlHead();
            int index = 1;
            List<LazyDynaBean> lazyDynaBeans = map.get(bean);
            for (LazyDynaBean dynaBean : lazyDynaBeans) {
                builder.append(contentHtmlBody(index, dynaBean.get("userName").toString(), dynaBean.get("userAccount").toString(),
                    dynaBean.get("userPassword").toString(), dynaBean.get("productStr").toString(),
                    dynaBean.get("clientId") == null ? "" : dynaBean.get("clientId").toString(),
                    dynaBean.get("clientSecret") == null ? "" : dynaBean.get("clientSecret").toString()));
                index++;
            }
            builder.append(contentHtmlFoot());
            LazyDynaBean lazyDynaBean = new LazyDynaBean();
            lazyDynaBean.set("context", builder);
            List<String> emailList = new ArrayList<>();
            emailList.add(bean);
            lazyDynaBean.set("emails", emailList);
            lazyDynaBean.set("userName", "");
            mailBean.add(lazyDynaBean);
        }
        sendMail(version, subject, template, title, mailBean);
    }

    private StringBuilder contentHtmlHead() {
        StringBuilder builder = new StringBuilder("<html><head></head><body>");
        builder.append("<div style=\"border-width:1px;width: 100%; color:#515a6e; border-style:solid; border-color:#dcdee2; background-color:#ffffff;\">");
        builder.append("<ul style=\"width: 100%;list-style: none;font-size:11px;font-weight:700;padding: 0px;margin: 0px;background-color: #f8f8f9;\">");
        builder.append("<li style=\"width: 8%;box-sizing: border-box;float: left;height: 36px;line-height:36px;border-right: 1px solid #dcdee2;padding: 0px 8px;\">序号</li>");
        builder.append("<li style=\"width: 20%;box-sizing: border-box;float: left;height: 36px;line-height:36px;border-right: 1px solid #dcdee2;padding: 0px 8px;\">名称</li>");
        builder.append("<li style=\"width: 22%;box-sizing: border-box;float: left;height: 36px;line-height:36px;border-right: 1px solid #dcdee2;padding: 0px 8px;\">管理员账号</li>");
        builder.append("<li style=\"width: 12%;box-sizing: border-box;float: left;height: 36px;line-height:36px;border-right: 1px solid #dcdee2;padding: 0px 8px;\">账号密码</li>");
        builder.append("<li style=\"width: 15%;box-sizing: border-box;float: left;height: 36px;line-height:36px;border-right: 1px solid #dcdee2;padding: 0px 8px;\">开通产品</li>");
        builder.append("<li style=\"width: 23%;box-sizing: border-box;float: left;height: 36px;line-height:36px;padding: 0px 8px;\">开放平台资料</li>");
        builder.append("<li style=\"clear:both;\"></li></ul>");
        return builder;
    }

    private StringBuilder contentHtmlBody(int index, String orgName, String account, String password,
        String productStr, String clientId, String clientSecret) {
        StringBuilder builder = new StringBuilder();
        builder.append("<ul style=\"width: 100%;list-style: none;font-size:11px;padding: 0px;margin: 0px;border-top: 1px solid #dcdee2;display: flex;\">");
        builder.append("<li style=\"width: 8%;box-sizing: border-box;float: left;border-right: 1px solid #dcdee2;padding:8px;word-wrap: break-word;\">" + index + "</li>");
        builder.append("<li style=\"width: 20%;box-sizing: border-box;float: left;border-right: 1px solid #dcdee2;padding:8px;word-wrap: break-word;\">" + orgName + "</li>");
        builder.append("<li style=\"width: 22%;box-sizing: border-box;float: left;border-right: 1px solid #dcdee2;padding:8px;word-wrap: break-word;\">" + account + "</li>");
        builder.append("<li style=\"width: 12%;box-sizing: border-box;float: left;border-right: 1px solid #dcdee2;padding:8px;word-wrap: break-word;\">" + password + "</li>");
        builder.append("<li style=\"width: 15%;box-sizing: border-box;float: left;border-right: 1px solid #dcdee2;padding:8px;word-wrap: break-word;\">" + productStr + "</li>");
        builder.append("<li style=\"width: 23%;box-sizing: border-box;float: left;padding:8px;word-wrap: break-word;\">clientId: " + clientId + "<br>clientSecret: " + clientSecret + "</li>");
        builder.append("<li style=\"clear:both;\"></li></ul>");
        return builder;
    }

    private StringBuilder contentHtmlFoot() {
        return new StringBuilder("</div></body></html>");
    }
}
