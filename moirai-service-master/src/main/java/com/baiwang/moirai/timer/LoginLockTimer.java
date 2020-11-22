package com.baiwang.moirai.timer;

import com.alibaba.fastjson.JSONObject;
import com.baiwang.moirai.common.Constants;
import com.baiwang.moirai.mapper.MoiraiUserMapper;
import com.baiwang.moirai.model.tenant.MoiraiTenantConfig;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.model.user.MoiraiUserCondition;
import com.baiwang.moirai.service.PasswordPolicyService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Component
public class LoginLockTimer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("redisTrafficTemplate")
    private RedisTemplate redisTemplate;

    @Autowired
    private MoiraiUserMapper moiraiUserMapper;

    @Autowired
    private PasswordPolicyService passwordPolicyService;

    private Integer totalRetryCount = 10;//总共尝试次数

    /**
     * <B>方法名称：</B><BR>
     * <B>概要说明：</B>实例化锁定的用户账号信息<BR>
     *
     * @return
     * @since 2019/10/24
     */
//    @Scheduled(cron = "0 0 4 * * ?")
    public void delLockUser() {
        try {
            setTotalRetryCount();
            Set keys = redisTemplate.keys(Constants.LOGINRETRYCOUNT + "*");
            logger.info("delLockUser keys = {}", JSONObject.toJSONString(keys));
            Iterator iterator = keys.iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                Object o = redisTemplate.opsForValue().get(key);
                if (o != null) {
                    String loginTry = (String) o;
                    if (totalRetryCount == Integer.valueOf(loginTry)) {
                        logger.info("delLockUser keys = {}", JSONObject.toJSONString(key));
                        redisTemplate.delete(key);
                        MoiraiUserCondition moiraiUser = new MoiraiUserCondition();
                        String userAccount = key.split(":")[2];
                        moiraiUser.setUseFlag("Y");
                        moiraiUser.setUserAccount(userAccount);
                        moiraiUserMapper.updateByCondition(moiraiUser);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("LoginLockTimer 定时器出现异常 Exception = {}", e);
        }
    }

    /**
     * 定时将处于锁定状态的用户信息置为正常状态
     *
     * @author LC
     * @date 10:26 2020/1/19
     **/
    @Scheduled(cron = "0 0 4 * * ?")
    public void delLockUser2() {
        logger.info("定时将处于锁定状态的用户信息置为正常状态任务开始！");
        Long start = System.currentTimeMillis();
        MoiraiUserCondition moiraiUser = new MoiraiUserCondition();
        moiraiUser.setUseFlag(Constants.flag_L);
        Long total = 1L;
        do {
            Page page = PageHelper.startPage(1, 100);
            List<MoiraiUser> userList = moiraiUserMapper.findUserListByCondition(moiraiUser);
            total = page.getTotal();
            logger.info("待解锁用户数 {}", total);
            for (int i = 0; i < userList.size(); i++) {
                MoiraiUser updateUser = new MoiraiUser();
                updateUser.setUserId(userList.get(i).getUserId());
                updateUser.setUseFlag(Constants.flag_Y);
                moiraiUserMapper.updateByPrimaryKeySelective(updateUser);
            }
        } while (total > 0);
        logger.info("定时解锁完成！共花费 {} ms", System.currentTimeMillis() - start);
    }

    /**
     * 动态设置尝试次数阈值
     */
    private void setTotalRetryCount() {
        try {
            List<MoiraiTenantConfig> configList = passwordPolicyService.getPasswordPolicyConfigList(null);
            for (int i = 0; i < configList.size(); i++) {
                if ("lockThreshold".equals(configList.get(i).getDetailCode())) {
                    totalRetryCount = Integer.valueOf(configList.get(i).getDetailValue());
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("动态设置尝试次数阈值失败", e);
        }
    }

}
