package com.baiwang.moirai.serviceimpl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baiwang.moirai.common.Constants;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.feignclient.SeqnumFeignClient;
import com.baiwang.moirai.mapper.MoiraiUserHistoryPasswordMapper;
import com.baiwang.moirai.mapper.MoiraiUserMapper;
import com.baiwang.moirai.model.sys.SysDict;
import com.baiwang.moirai.model.tenant.MoiraiTenantConfig;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.model.user.MoiraiUserHistoryPassword;
import com.baiwang.moirai.service.MoiraiTenantConfigService;
import com.baiwang.moirai.service.PasswordPolicyService;
import com.baiwang.moirai.service.SysDictService;
import com.baiwang.moirai.utils.AdminUtils;
import com.baiwang.moirai.utils.DateTimeUtils;
import com.baiwang.moirai.utils.RegularExpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 密码策略
 *
 * @author LC
 * @date 2019/12/6 15:59
 */
@Service
public class PasswordPolicyServiceImpl implements PasswordPolicyService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordPolicyServiceImpl.class);

    @Autowired
    private MoiraiUserHistoryPasswordMapper historyPasswordMapper;

    @Autowired
    private MoiraiUserMapper moiraiUserMapper;

    @Autowired
    private MoiraiTenantConfigService moiraiTenantConfigService;

    @Autowired
    private SeqnumFeignClient seqnumFeignClient;

    @Autowired
    private SysDictService sysDictService;

    /**
     * 获取租户密码策略配置列表
     *
     * @param tenantId
     */
    @Override
    public List<MoiraiTenantConfig> getPasswordPolicyConfigList(Long tenantId) {
        // 获取默认配置信息
        List<MoiraiTenantConfig> list = getDefaultConfigList();

        // 从租户配置表中查询数据
        if (tenantId != null) {
            List<MoiraiTenantConfig> exit = moiraiTenantConfigService.getConfigListCache(tenantId, null);
            if (exit != null && !exit.isEmpty()) {
                list.forEach(item -> {
                    exit.forEach(item2 -> {
                        MoiraiTenantConfig config = JSON.parseObject(JSONObject.toJSONString(item2), MoiraiTenantConfig.class);
                        if (item.getDetailCode().equals(config.getDetailCode())) {
                            item.setDetailValue(config.getDetailValue());
                        }
                    });
                });
                return list;
            }
        }

        // 租户配置表中为空，从系统字典中获取平台配置信息
        List<SysDict> details = sysDictService.getSysDictCacheByDictType(Constants.PASSWORD_POLICY);
        list.forEach(item -> {
            details.forEach(item2 -> {
                if (item.getDetailCode().equals(item2.getDictCode())) {
                    item.setDetailValue(item2.getDictName());
                }
            });
        });
        return list;
    }

    /**
     * 是否需要校验首次登陆
     * true-需要校验首次登陆（默认）
     * false-不需要校验首次登陆
     *
     * @param tenantId
     * @return
     */
    @Override
    public Boolean checkFirstLogin(Long tenantId) {
        List<MoiraiTenantConfig> configList = getPasswordPolicyConfigList(tenantId);
        for (int i = 0; i < configList.size(); i++) {
            if (Constants.FIRST_LOGIN_CHECK.equals(configList.get(i).getDetailCode())) {
                return Boolean.valueOf(configList.get(i).getDetailValue());
            }
        }
        return true;
    }

    /**
     * 校验用户密码是否超过有效期
     *
     * @param tenantId 租户id
     * @param userId   用户id
     */
    @Override
    public Boolean checkPwdOverdue(Long tenantId, Long userId) {
        // 4 密码有效期超过{passwordValidity}天，重新设置密码 0-不限制（默认），验证用户名、旧密码、新密码、图片验证码正确后登陆成功
        List<MoiraiTenantConfig> configList = getPasswordPolicyConfigList(tenantId);
        Integer passwordValidity = 0;
        for (int i = 0; i < configList.size(); i++) {
            if (Constants.PWD_VALIDITY.equals(configList.get(i).getDetailCode())) {
                passwordValidity = Integer.valueOf(configList.get(i).getDetailValue());
            }
        }
        if (0 < passwordValidity) {
            String msg = MessageFormat.format("当前密码已经超过密码有效期（{0}天），请重新设置密码！", passwordValidity);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.DATE, -passwordValidity);
            List<MoiraiUserHistoryPassword> list = historyPasswordMapper.selectListByUserId(userId);
            if (list == null || list.isEmpty()) {
                // 数据为空， 判断用户创建时间是否超过密码有效期
                MoiraiUser moiraiUser = moiraiUserMapper.selectByPrimaryKey(userId);
                Long createTime = moiraiUser.getCreateTime();
                Long nowTime = Long.parseLong(DateTimeUtils.toDateTimeString("yyyyMMddHHmmssSSS", calendar));
                if (createTime.compareTo(nowTime) < 0) {
                    logger.info("{} 用户id：{}", msg, userId);
                    return true;
                }
                return false;
            }
            // 当用户密码时间超过指定天数时，需要校验新密码和图片验证码
            if (list.get(0).getCreateTime().compareTo(calendar.getTime()) < 0) {
                logger.info("{} 用户id：{}", msg, userId);
                return true;
            }
        }
        return false;
    }

    /**
     * 校验注册密码策略
     *
     * @param tenantId 租户id
     * @param password 明文用户密码
     * @param account  用户名称/电话
     */
    @Override
    public Boolean checkRegisterPolicy(Long tenantId, String password, String account) {
        List<MoiraiTenantConfig> configList = getPasswordPolicyConfigList(tenantId);
        // 密码中不能包含账号`{pwdNoAccountCheck}`。true-校验， false-不校验（默认）
        Boolean pwdNoAccountCheck = false;
        // 密码长度{pwdLength}，最小1位，最大20位
        Integer pwdLength = 8;
        // 包含数字、小写英文字符、大写英文字符、特殊字符的任意{pwdTypeSize}种组合
        Integer pwdTypeSize = 3;
        for (int i = 0; i < configList.size(); i++) {
            if (Constants.PWD_TYPE_SIZE.equals(configList.get(i).getDetailCode())) {
                pwdTypeSize = Integer.valueOf(configList.get(i).getDetailValue());
                continue;
            }
            if (Constants.PWD_LENGTH.equals(configList.get(i).getDetailCode())) {
                pwdLength = Integer.valueOf(configList.get(i).getDetailValue());
                continue;
            }
            if (Constants.PWD_NO_ACCOUNT_CHECK.equals(configList.get(i).getDetailCode())) {
                pwdNoAccountCheck = Boolean.valueOf(configList.get(i).getDetailValue());
            }
        }
        // 密码长度最小为1 最大为20
        pwdLength = pwdLength < 1 ? 1 : (pwdLength > 20 ? 20 : pwdLength);
        // 最小为0 最大为4
        pwdTypeSize = pwdTypeSize < 0 ? 0 : pwdTypeSize > 4 ? 4 : pwdTypeSize;
        Boolean check = RegularExpUtils.checkPasswordRule(password, account, pwdTypeSize, pwdLength, pwdNoAccountCheck);
        if (!check) {
            StringBuilder sb = new StringBuilder();
            sb.append(MessageFormat.format("密码长度必须在{0}到20位之间,", pwdLength));
            if (pwdNoAccountCheck) {
                sb.append("不能包含账号,");
            }
            sb.append(MessageFormat.format("至少包含大写字母、小写字母、数字、特殊字符中的{0}种", pwdTypeSize));
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PASSWORD_ERROR.getCode(), sb.toString());
        }
        return check;
    }

    /**
     * 校验更新用户密码策略
     *
     * @param tenantId    租户id
     * @param userId      用户id
     * @param newPassword 明文新密码
     */
    @Override
    public void checkUpdatePolicy(Long tenantId, Long userId, String newPassword) {
        List<MoiraiTenantConfig> configList = getPasswordPolicyConfigList(tenantId);
        // 校验历史密码是否重复
        // 2 最近{historySize}次历史密码重复校验限制 0-不限制（默认），5-最近5次密码不能相同(不包括当前旧密码)
        int historySize = 0;
        for (int i = 0; i < configList.size(); i++) {
            if (Constants.HISTORY_SIZE.equals(configList.get(i).getDetailCode())) {
                historySize = Integer.valueOf(configList.get(i).getDetailValue());
                break;
            }
        }
        if (0 < historySize) {
            List<MoiraiUserHistoryPassword> list = historyPasswordMapper.selectListByUserId(userId);
            for (int i = 0; i < list.size(); i++) {
                if (i < historySize) {
                    String uuid = list.get(0).getUuid();
                    String passwd = AdminUtils.getUuidPasswd(newPassword, uuid);
                    if (list.get(i).getPassword().equals(passwd)) {
                        throw new MoiraiException(MoiraiErrorEnum.MOIRAI_HISTORY_PWD_REPEAT.getCode(),
                                MessageFormat.format(MoiraiErrorEnum.MOIRAI_HISTORY_PWD_REPEAT.getMsg(), historySize));
                    }
                } else {
                    // 删除多余的密码
                    historyPasswordMapper.deleteByPrimaryKey(list.get(i).getId());
                }
            }
        } else {
            // 当不设置密码有效期时，仅保留最近10次密码
            List<MoiraiUserHistoryPassword> list = historyPasswordMapper.selectListByUserId(userId);
            for (int i = 0; i < list.size(); i++) {
                if (i >= 10) {
                    // 删除多余的密码
                    historyPasswordMapper.deleteByPrimaryKey(list.get(i).getId());
                }
            }
        }
    }

    /**
     * 存储历史密码
     *
     * @param moiraiUser
     */
    @Override
    public void addHistoryPassword(MoiraiUser moiraiUser) {
        // 将旧密码存于历史密码表中
        MoiraiUserHistoryPassword historyPassword = new MoiraiUserHistoryPassword();
        historyPassword.setId(seqnumFeignClient.getNum(Constants.MOIRAI_USER_HISTORY_PASSWORD));
        historyPassword.setCreateTime(new Date());
        historyPassword.setUserId(moiraiUser.getUserId());
        historyPassword.setPassword(moiraiUser.getUserPassword());
        historyPassword.setUuid(moiraiUser.getUuid());
        historyPasswordMapper.insert(historyPassword);
    }

    /**
     * 获取默认配置信息
     */
    private List<MoiraiTenantConfig> getDefaultConfigList() {
        List<MoiraiTenantConfig> result = new ArrayList<>(8);
        // 登陆失败`{lockThreshold}`次，锁定用户。 0-不锁定，10-十次（默认）
        MoiraiTenantConfig lockThreshold = new MoiraiTenantConfig();
        lockThreshold.setDetailCode(Constants.LOCK_THRESHOLD);
        lockThreshold.setDetailValue("10");
        result.add(lockThreshold);

        // 登陆失败`{checkThreshold}`次，验证验证码。 0-校验码必填，3-三次（默认）
        MoiraiTenantConfig checkThreshold = new MoiraiTenantConfig();
        checkThreshold.setDetailCode(Constants.CHECK_THRESHOLD);
        checkThreshold.setDetailValue("3");
        result.add(checkThreshold);

        // 首次登陆`{firstLoginCheck}` 修改密码。 true-需要（默认），false-不需要
        MoiraiTenantConfig firstLoginCheck = new MoiraiTenantConfig();
        firstLoginCheck.setDetailCode(Constants.FIRST_LOGIN_CHECK);
        firstLoginCheck.setDetailValue("true");
        result.add(firstLoginCheck);

        // 密码有效期超过`{pwdValidity}`天，重新设置密码。 0-不限制（默认），验证用户名、旧密码、新密码、图片验证码正确后登陆成功
        MoiraiTenantConfig passwordValidity = new MoiraiTenantConfig();
        passwordValidity.setDetailCode(Constants.PWD_VALIDITY);
        passwordValidity.setDetailValue("0");
        result.add(passwordValidity);

        // 密码中不能包含账号`{pwdNoAccountCheck}`。true-校验， false-不校验（默认）
        MoiraiTenantConfig passwordNoAccountCheck = new MoiraiTenantConfig();
        passwordNoAccountCheck.setDetailCode(Constants.PWD_NO_ACCOUNT_CHECK);
        passwordNoAccountCheck.setDetailValue("false");
        result.add(passwordNoAccountCheck);

        // 包含以下4种字符类型的任意{pwdTypeSize}种组合，3-三种（默认）
        MoiraiTenantConfig pwdTypeSize = new MoiraiTenantConfig();
        pwdTypeSize.setDetailCode(Constants.PWD_TYPE_SIZE);
        pwdTypeSize.setDetailValue("3");
        result.add(pwdTypeSize);

        //密码长度{pwdLength}，最小1位，最大20位，8-八位（默认）
        MoiraiTenantConfig pwdLength = new MoiraiTenantConfig();
        pwdLength.setDetailCode(Constants.PWD_LENGTH);
        pwdLength.setDetailValue("8");
        result.add(pwdLength);

        // 最近`{historySize}`次历史密码重复校验限制。0-不限制（默认），5-最近5次密码不能相同（不包括当前旧密码）
        MoiraiTenantConfig historySize = new MoiraiTenantConfig();
        historySize.setDetailCode(Constants.HISTORY_SIZE);
        historySize.setDetailValue("0");
        result.add(historySize);

        return result;
    }
}
