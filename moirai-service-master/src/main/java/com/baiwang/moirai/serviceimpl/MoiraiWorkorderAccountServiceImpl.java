package com.baiwang.moirai.serviceimpl;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.common.Constants;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.feignclient.SeqnumFeignClient;
import com.baiwang.moirai.mapper.MoiraiOrgMapper;
import com.baiwang.moirai.mapper.MoiraiTenantMapper;
import com.baiwang.moirai.mapper.MoiraiUserMapper;
import com.baiwang.moirai.mapper.MoiraiWorkorderAccountMapper;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.tenant.MoiraiTenant;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.model.user.MoiraiUserCondition;
import com.baiwang.moirai.model.workorder.MoiraiWorkorderAccount;
import com.baiwang.moirai.model.workorder.MoiraiWorkorderAccountCondition;
import com.baiwang.moirai.service.MoiraiWorkorderAccountService;
import com.baiwang.moirai.service.PasswordPolicyService;
import com.baiwang.moirai.service.PasswordService;
import com.baiwang.moirai.utils.AdminUtils;
import com.baiwang.moirai.utils.DateTimeUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.commons.beanutils.LazyDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.baiwang.moirai.enumutil.MoiraiErrorEnum.MOIRAI_DB_NULL;

/**
 * @author LC
 */
@Service
public class MoiraiWorkorderAccountServiceImpl implements MoiraiWorkorderAccountService {
    private static final Logger logger = LoggerFactory.getLogger(MoiraiWorkorderAccountServiceImpl.class);

    @Autowired
    private MoiraiWorkorderAccountMapper workorderMapper;

    @Autowired
    private MoiraiOrgMapper orgMapper;

    @Autowired
    private MoiraiTenantMapper tenantMapper;

    @Autowired
    private MoiraiUserMapper userMapper;

    @Autowired
    private SeqnumFeignClient seqnumFeignClient;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private PasswordPolicyService passwordPolicyService;

    /**
     * 租户找回密码
     *
     * @param workorderPwd
     */
    @Override
    public void backPassword(MoiraiWorkorderAccount workorderPwd) {
        MoiraiOrg queryOrg = new MoiraiOrg();
        queryOrg.setTaxCode(workorderPwd.getTaxCode());
        MoiraiOrg exOrg = orgMapper.selectOneOrg(queryOrg);
        if (exOrg == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_BACK_PASSWORD_NO_ORG_ERROR);
        }
        if (exOrg.getParentOrg() != 0){
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_BACK_ORG_NOT_TOP_ERROR);
        }
        if (!exOrg.getOrgName().equals(workorderPwd.getTenantName())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_BACK_PASSWORD_NOT_CORR_ERROR);
        }

        MoiraiWorkorderAccountCondition query = new MoiraiWorkorderAccountCondition();
        query.setTaxCode(workorderPwd.getTaxCode());
        query.setAuditStatus("0");
        List<MoiraiWorkorderAccount> exList = workorderMapper.queryList(query);
        if (!exList.isEmpty()) {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_MONTH,-2);
            if (exList.get(0).getCreateTime().compareTo(c.getTime()) > 0) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_APPLY_EXISTS_ERROR);
            }
        }

        workorderPwd.setId(seqnumFeignClient.getSeq().toString());
        workorderPwd.setAuditStatus(Constants.DEFAULT_ZERO);
        workorderPwd.setCreateTime(new Date());
        workorderPwd.setAuditMsg(null);
        workorderPwd.setAuditTime(null);
        workorderMapper.insertSelective(workorderPwd);
    }

    @Override
    public BWJsonResult<MoiraiWorkorderAccount> queryList(MoiraiWorkorderAccountCondition workorderPwd) {
        Page<Object> page = PageHelper.startPage(workorderPwd.getPageNo(), workorderPwd.getPageSize());
        List<MoiraiWorkorderAccount> workorderPwdList = workorderMapper.queryList(workorderPwd);
        return new BWJsonResult<>(workorderPwdList, (int) page.getTotal());
    }

    @Override
    @Transactional
    public int updateWorkorder(MoiraiWorkorderAccount workorder) {
        MoiraiWorkorderAccount order = workorderMapper.selectByPrimaryKey(Long.valueOf(workorder.getId()));
        if (order == null) {
            throw new MoiraiException(MOIRAI_DB_NULL.getCode(), "工单" + MOIRAI_DB_NULL.getMsg());
        }
        if (!order.getTaxCode().equals(workorder.getTaxCode())) {
            throw new MoiraiException(MOIRAI_DB_NULL.getCode(), "税号" + MOIRAI_DB_NULL.getMsg());
        }
        MoiraiOrg queryOrg = new MoiraiOrg();
        queryOrg.setTaxCode(workorder.getTaxCode());
        queryOrg.setOrgName(workorder.getTenantName());
        MoiraiOrg org = orgMapper.selectOneOrg(queryOrg);
        if (org == null) {
            throw new MoiraiException(MOIRAI_DB_NULL.getCode(), "机构" + MOIRAI_DB_NULL.getMsg());
        }
        order.setAuditMsg(workorder.getAuditMsg());
        order.setAuditStatus(workorder.getAuditStatus());
        order.setAuditTime(new Date());
        order.setAuditUser(workorder.getAuditUser());
        int i = workorderMapper.updateByPrimaryKeySelective(order);
        MoiraiTenant moiraiTenant = tenantMapper.selectByPrimaryKey(org.getTenantId());
        String status = workorder.getAuditStatus();
        String context;
        String userName = "";
        if (Constants.DEFAULT_ONE.equals(status)) {
            if (!workorder.getContactEmail().equals(moiraiTenant.getTenantEmail())) {
                moiraiTenant.setTenantEmail(workorder.getContactEmail());
                tenantMapper.updateTenant(moiraiTenant);
            }
            // 重置顶级机构管理员密码
            MoiraiUserCondition condition = new MoiraiUserCondition();
            condition.setTenantId(org.getTenantId());
            condition.setOrgId(org.getOrgId());
            condition.setUserCreatetype(Constants.DEFAULT_ZERO);
            List<MoiraiUser> userList = userMapper.findUserByCondition(condition);
            if (userList.isEmpty()) {
                throw new MoiraiException(MOIRAI_DB_NULL.getCode(), "顶级机构管理员账号" + MOIRAI_DB_NULL.getMsg());
            }
            MoiraiUser user = userList.get(0);
            // 存储旧密码到历史密码表中
            passwordPolicyService.addHistoryPassword(user);
            String password = passwordService.calculatePassword(Constants.MOIRAI_VERSION_V2);
            String newyhMm = AdminUtils.getUuidPasswd(password, user.getUuid());
            user.setUserPassword(newyhMm);
            user.setModifyUser(workorder.getAuditUser());
            user.setFirstLogin(Constants.flag_Y);
            user.setModifyTime(DateTimeUtils.nowTimeLong());
            user.setUserEmail(workorder.getContactEmail());
            userMapper.updateByPrimaryKeySelective(user);
            userName = moiraiTenant.getTenantName();
            context = "您的百望云企业管理员账号找回申请已经通过.<br>&nbsp;&nbsp;&nbsp;&nbsp;" + "您的初始化帐号：" + user.getUserAccount() + "<br>&nbsp;&nbsp;&nbsp;&nbsp;您的初始化密码：" + password;
        } else if (Constants.DEFAULT_TWO.equals(status)) {
            userName = order.getContactName();
            context = "您的百望云企业管理员账号找回申请已经被驳回.<br>&nbsp;&nbsp;&nbsp;&nbsp;具体原因说明:" + workorder.getAuditMsg();
        } else {
            context = "未审核";
        }
        List<LazyDynaBean> lazyDynaBeans = new ArrayList<>();
        List<String> emails = new ArrayList<>();
        emails.add(workorder.getContactEmail());
        LazyDynaBean lazyDynaBean = new LazyDynaBean();
        lazyDynaBean.set("emails", emails);
        lazyDynaBean.set("context", context);
        lazyDynaBean.set("userName", userName);
        lazyDynaBeans.add(lazyDynaBean);
        passwordService.sendMail(Constants.MOIRAI_VERSION_V2, "百望云申请通知", "ZC_VERIFY", "百望云企业管理员账号找回申请通知", lazyDynaBeans);
        return i;
    }
}
