package com.baiwang.moirai.serviceimpl;

import com.alibaba.fastjson.JSONObject;
import com.baiwang.cloud.common.enumutil.ErrorType;
import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.cloud.common.model.ErrorMessage;
import com.baiwang.moirai.common.Constants;
import com.baiwang.moirai.common.WebContext;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.feignclient.SeqnumFeignClient;
import com.baiwang.moirai.mapper.MoiraiOrgMapper;
import com.baiwang.moirai.mapper.MoiraiUserMapper;
import com.baiwang.moirai.mapper.MoiraiUserMemberMapper;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.tenant.MoiraiTenantVO;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.model.user.MoiraiUserCondition;
import com.baiwang.moirai.model.user.MoiraiUserMember;
import com.baiwang.moirai.model.user.MoiraiUserMemberCondition;
import com.baiwang.moirai.service.*;
import com.baiwang.moirai.utils.DateTimeUtils;
import com.baiwang.moirai.utils.RegularExpUtils;
import com.baiwang.moirai.utils.StrUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author LC
 * @date 2019/12/3 20:15
 */
@Service
public class MoiraiUserMemberServiceImpl implements MoiraiUserMemberService {

    private static final Logger logger = LoggerFactory.getLogger(MoiraiUserMemberServiceImpl.class);

    @Autowired
    private MoiraiUserMapper moiraiUserMapper;

    @Autowired
    private MoiraiUserPwdService moiraiUserPwdService;

    @Autowired
    private MoiraiUserService moiraiUserService;

    @Autowired
    private MoiraiTenantService moiraiTenantService;

    @Autowired
    private MoiraiUserMemberMapper moiraiUserMemberMapper;

    @Autowired
    private SeqnumFeignClient seqnumFeignClient;

    @Autowired
    private MoiraiOrgService moiraiOrgService;

    @Autowired
    private MoiraiOrgMapper moiraiOrgMapper;

    /**
     * 发送短信验证码
     *
     * @param phone
     */
    @Override
    public void sendPhoneCode(String phone, String account, String cert) {
        logger.info("绑定企业用户发送手机验证码，phone：{}, 待绑定账号：{}，滑块验证码：{}", phone, account, cert);
        // 校验手机号
        if (!RegularExpUtils.checkMobile(phone)) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_TELEPHONE_ERROR);
        }
        if (StringUtils.isBlank(account)) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_BINDB_ACCOUNT_NULL_ERROR);
        }
        // 根据账号查询待绑定用户信息
        MoiraiUser bUser = moiraiUserMapper.getUserByUserAccount(account);
        if (bUser == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_BINDB_NOT_ACCOUNT_ERROR);
        }
        // 校验被绑定账号是否是B端账号
        if (!"B".equalsIgnoreCase(bUser.getUserType())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_BINDB_NOT_B_ERROR);
        }
        // 查询租户信息
        MoiraiTenantVO moiraiTenantVO = moiraiTenantService.getCacheMoiraiTenant(bUser.getTenantId());

        // 1、	当前待绑定账号所在租户未开启双因子或开启双因子邮箱认证
        // 验证用户名和密码正确、验证验证码正确即可将手机号和账号绑定；

        // 2、	当前待绑定账号所在租户开启双因子手机短信认证
        if (Constants.DEFAULT_ONE.equals(moiraiTenantVO.getDualFactor())) {
            if (StringUtils.isBlank(bUser.getTelephone())) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_BINDB_NOT_SET_PHONE_ERROR);
            }
            // 校验和当前登陆人手机号是否一致
            if (!phone.equals(bUser.getTelephone())) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_BINDB_EQUALS_PHONE_ERROR);
            }
        }
        MoiraiUserCondition moiraiUserCondition = new MoiraiUserCondition();
        moiraiUserCondition.setUserType(Constants.USER_TYPE_C);
        moiraiUserCondition.setTelephone(phone);
        moiraiUserCondition.setCert(cert);
        moiraiUserCondition.setComeFrom(Constants.DEFAULT_ONE);
        // 发送短信
        moiraiUserPwdService.smsForPwd(moiraiUserCondition);
    }

    /**
     * 绑定企业信息
     *
     * @param moiraiUserCondition 待绑定信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BWJsonResult bindTenant(MoiraiUserCondition moiraiUserCondition) {
        MoiraiUser cUser = moiraiUserMapper.selectByPrimaryKey(moiraiUserCondition.getUserId());
        logger.info("绑定企业用户信息，待绑定信息入参：{}，当前登陆人信息：{}", moiraiUserCondition, cUser);
        // 1 必填项校验
        if (StringUtils.isBlank(moiraiUserCondition.getTelephone())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_BINDB_PHONE_NULL_ERROR);
        }
        if (StringUtils.isBlank(moiraiUserCondition.getUserAccount())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_BINDB_ACCOUNT_NULL_ERROR);
        }
        if (StringUtils.isBlank(moiraiUserCondition.getUserPassword())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_BINDB_PASSWORD_NULL_ERROR);
        }
        if (!"C".equalsIgnoreCase(cUser.getUserType())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_BINDB_ONLY_C_ERROR);
        }
        // 校验短信验证码是否正确
        if (StringUtils.isNotBlank(moiraiUserCondition.getSmsCode())) {
            MoiraiUserCondition checkSMS = new MoiraiUserCondition();
            checkSMS.setSmsCode(moiraiUserCondition.getSmsCode());
            checkSMS.setTelephone(moiraiUserCondition.getTelephone());
            moiraiUserPwdService.checkSMSCode(checkSMS);
        }
        // 根据账号查询待绑定用户信息
        MoiraiUser bUser = moiraiUserMapper.getUserByUserAccount(moiraiUserCondition.getUserAccount());

        // 查询租户信息
        MoiraiTenantVO bTenantVO = moiraiTenantService.getCacheMoiraiTenant(bUser.getTenantId());

        if (bTenantVO == null || Constants.flag_N.equals(bTenantVO.getUseFlag())) {
            return new BWJsonResult(new MoiraiException(MoiraiErrorEnum.MOIRAI_TENANT_LOCK));
        }
        //暂时只判断所在机构是否锁定
        MoiraiOrg moiraiOrg = moiraiOrgService.selectByOrgId(bUser.getOrgId());
        if (moiraiOrg == null || Constants.flag_N.equals(moiraiOrg.getUseFlag())) {
            return new BWJsonResult(new MoiraiException(MoiraiErrorEnum.MOIRAI_TENANT_LOCK));
        }
        //锁定
        if (Constants.flag_L.equals(bUser.getUseFlag())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_LOCK_ERROR);
        }
        //冻结
        if (Constants.flag_N.equals(bUser.getUseFlag())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_FROZEN_ERROR);
        }
        // 校验密码
        boolean checkPassword = moiraiUserService.checkPassword(bUser, moiraiUserCondition.getUserPassword(),
                moiraiUserCondition.getPasswordTrans(), moiraiUserCondition.getPasswordCheckType());
        if (!checkPassword) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_PASSWORD_MISS_ERROR);
        }

        MoiraiUser phoneUser = moiraiUserMapper.getUserByTelephone(moiraiUserCondition.getTelephone(), bUser.getTenantId());
        if (phoneUser != null && !phoneUser.getUserId().equals(bUser.getUserId())){
            throw new MoiraiException("3037", "该电话已被企业其他用户使用，无法绑定！");
        }

        // 查询已经绑定的所有企业用户信息
        MoiraiUserMemberCondition queryMember = new MoiraiUserMemberCondition();
        queryMember.setBindUserId(bUser.getUserId());
        List<MoiraiUserMember> memberList = moiraiUserMemberMapper.selectByBean(queryMember);
        if (!memberList.isEmpty()){
            throw new MoiraiException("3037", "该企业账号已经被其他个人账号绑定！");
        }

        queryMember.setBindUserId(null);
        queryMember.setUserId(cUser.getUserId());
        memberList = moiraiUserMemberMapper.selectByBean(queryMember);
        // 构造moiraiUserMember 信息
        MoiraiUserMember newMember = new MoiraiUserMember();
        newMember.setMemberId(seqnumFeignClient.getNum(Constants.MOIRAI_USER_MEMBER));
        newMember.setTenantId(bUser.getTenantId());
        newMember.setOrgId(bUser.getOrgId());
        newMember.setUserId(cUser.getUserId());
        newMember.setBindUserId(bUser.getUserId());
        newMember.setDefaultOrgFlag(Constants.flag_N);
        newMember.setDefaultValidationFlag(Constants.flag_N);
        Long nowTime = DateTimeUtils.nowTimeLong();
        newMember.setCreateTime(new Date());
        newMember.setUpdateTime(new Date());
        newMember.setOrderNum(memberList == null ? 1 : memberList.size() + 1);
        newMember.setDelFlag(Constants.flag_N);
        newMember.setUpdateUser(cUser.getUserId() + "");

        // 默认没有开启双因子的企业信息
        boolean hasFirstB = false;
        // 个人手机号绑定的第一个企业租户，设置为个人手机号登录的默认租户。
        if (memberList.isEmpty()) {
            newMember.setDefaultOrgFlag(Constants.flag_Y);
            // 个人手机号绑定的第一个开启双因子认证租户的账号，那么使用该账号的验证手机号或邮箱作为该个人手机号二次认证接受验证码的默认手机号或邮箱；
            if (!Constants.DEFAULT_ZERO.equals(bTenantVO.getDualFactor())) {
                newMember.setDefaultValidationFlag(Constants.flag_Y);
                hasFirstB = true;
            }
        }

        // 没有第一个开启双因子的租户
        for (int i = 0; i < memberList.size(); i++) {
            // 检测该企业账号是否已经绑定
            if (bUser.getUserId().equals(memberList.get(i).getBindUserId())) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_BINDING_REPEAT);
            }
            if (Constants.flag_Y.equals(memberList.get(i).getDefaultValidationFlag())) {
                hasFirstB = true;
            }
            if (bUser.getTenantId().equals(memberList.get(i).getTenantId())){
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_SAME_TENANT);
            }
        }

        // 之前绑定的租户没有开启双因子验证，且新绑定的租户开启了双因子验证，则设置为默认该待绑定企业账号未二次登陆账号
        if (!hasFirstB && !Constants.DEFAULT_ZERO.equals(bTenantVO.getDualFactor())) {
            newMember.setDefaultValidationFlag(Constants.flag_Y);
        }

        // 插入绑定关系记录表
        logger.info("插入绑定关系记录表，入参：{}", newMember);
        moiraiUserMemberMapper.insertSelective(newMember);

        MoiraiUser moiraiUser = new MoiraiUser();
        moiraiUser.setUserId(bUser.getUserId());
        moiraiUser.setTelephone(moiraiUserCondition.getTelephone());
        moiraiUser.setPhoneValidate(Constants.flag_Y);
        moiraiUser.setModifyTime(nowTime);
        moiraiUserMapper.updateByPrimaryKeySelective(moiraiUser);

        BWJsonResult bwJsonResult = new BWJsonResult();
        bwJsonResult.setMessage("绑定企业用户成功！");
        return bwJsonResult;
    }

    /**
     * <B>方法名称：</B>返回用户关联的租户列表<BR>
     * <B>概要说明：</B><BR>
     *
     * @return
     * @since 2019/12/5
     */
    public BWJsonResult getBindTenantInfo(MoiraiUserMemberCondition condition){
        logger.info("getBindTenantInfo 入参：" + JSONObject.toJSONString(condition));
        MoiraiUserMember moiraiUserMember = new MoiraiUserMember();
        Long userId = condition.getUserId();
        moiraiUserMember.setUserId(userId);
        Integer pageNo = condition.getPageNo();
        if(pageNo != 0){
            PageHelper.startPage(condition.getPageNo(), condition.getPageSize());
        }
        List<MoiraiUserMemberCondition> members = null;
        Long total = null;
        try{
            members = moiraiUserMemberMapper.selectByUnion(condition);
            if(members != null && members.size()>0){
                if(pageNo != 0){
                    PageInfo<MoiraiUserMemberCondition> pageInfo = new PageInfo<>(members);
                    total = pageInfo.getTotal();
                }
                List<MoiraiUser> users = new ArrayList<>();
                List<Long> orgs = new ArrayList<>();
                for(MoiraiUserMemberCondition userMember:members){
                    MoiraiUser moiraiUser = new MoiraiUser();
                    moiraiUser.setUserId(userMember.getBindUserId());
                    Long orgId = userMember.getOrgId();
                    users.add(moiraiUser);
                    orgs.add(orgId);
                }
                Map<String,List> orgMap = new HashMap<>();
                orgMap.put("orgId",orgs);
                List<MoiraiOrg> moiraiOrgs = moiraiOrgMapper.queryOrgListByCondition(orgMap);
                Map<Long,MoiraiOrg> moiraiOrgMap = new HashMap<>();
                for(MoiraiOrg moiraiOrg : moiraiOrgs){
                    Long orgId = moiraiOrg.getOrgId();
                    moiraiOrgMap.put(orgId,moiraiOrg);
                }
                List<MoiraiUser> users1 = moiraiUserMapper.selectByIds(users);
                Map<Long,MoiraiUser> map = new HashMap<>();
                for(MoiraiUser moiraiUser:users1){
                    map.put(moiraiUser.getUserId(),moiraiUser);
                }
                for(MoiraiUserMemberCondition userMember:members){
                    Long bindUserId = userMember.getBindUserId();
                    Long orgId = userMember.getOrgId();
                    MoiraiUser moiraiUser = map.get(bindUserId);
                    MoiraiOrg moiraiOrg = moiraiOrgMap.get(orgId);
                    String useFlag = moiraiOrg.getUseFlag();
                    userMember.setUserAccount(moiraiUser.getUserAccount());
                    userMember.setUserEmail(moiraiUser.getUserEmail());
                    userMember.setUseFlag(moiraiUser.getUseFlag());
                    userMember.setoUseFlag(useFlag);
                    userMember.setTaxCode(moiraiOrg.getTaxCode());
                }
            }

        }catch (Exception e){
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_GET_TENANT_LIST_ERROR;
            logger.error(new ErrorMessage(requestURI,errorEnum.getCode(),errorEnum.getMsg(), ErrorType.CustomerError).toString(),e);

            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_BINDB_ZH_ERROR);
        }

        BWJsonResult bwJsonResult = new BWJsonResult(members);
        if(total != null){
            bwJsonResult.setTotal(Integer.valueOf(total+""));
        }
        return bwJsonResult;
    }


    /**
     * <B>方法名称：</B><BR>
     * <B>概要说明：</B>设置默认登录机构功能：获取租户拥有的双因子租户信息列表<BR>
     *
     * @return
     * @since 2019/12/19
     */
    public List<MoiraiUserMemberCondition> getAuthTenant(MoiraiUserMemberCondition condition){

        List<MoiraiUserMemberCondition> rtnList = null;
        try{
            List<MoiraiUserMemberCondition>  members = moiraiUserMemberMapper.selectByUnion(condition);
            if(members != null && members.size()>0){
                List<MoiraiUser> users = new ArrayList<>();
                for(MoiraiUserMemberCondition userMember:members){
                    MoiraiUser moiraiUser = new MoiraiUser();
                    moiraiUser.setUserId(userMember.getBindUserId());
                    users.add(moiraiUser);
                }
                List<MoiraiUser> users1 = moiraiUserMapper.selectByIds(users);
                Map<Long,MoiraiUser> map = new HashMap<>();
                for(MoiraiUser moiraiUser:users1){
                    map.put(moiraiUser.getUserId(),moiraiUser);
                }
                rtnList = new ArrayList<>();
                for(MoiraiUserMemberCondition userMember:members){
                    String dualFactor = userMember.getDualFactor();
                    if("1".equals(dualFactor)){//手机号
                        Long bindUserId = userMember.getBindUserId();
                        MoiraiUser moiraiUser = map.get(bindUserId);
                        String telephone = moiraiUser.getTelephone();
                        String phoneValidate = moiraiUser.getPhoneValidate();
                        if(Constants.flag_Y.equals(phoneValidate)&& !StrUtils.isEmpty(telephone)){
                            userMember.setTelephone(telephone);
                            rtnList.add(userMember);
                        }
                    }else if("2".equals(dualFactor)){
                        Long bindUserId = userMember.getBindUserId();
                        MoiraiUser moiraiUser = map.get(bindUserId);
                        String userEmail = moiraiUser.getUserEmail();
                        if(!StrUtils.isEmpty(userEmail)){
                            userMember.setUserEmail(userEmail);
                            rtnList.add(userMember);
                        }
//                        String emailValidate = moiraiUser.getEmailValidate();
//                        if(Constants.flag_Y.equals(emailValidate)&& !StrUtils.isEmpty(userEmail)){
//                            userMember.setUserEmail(userEmail);
//                            rtnList.add(userMember);
//                        }

                    }else {

                    }
                }
            }
        }catch (Exception e){
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_GET_DUAL_FACTOR_EXCEPTION;
            logger.error(new ErrorMessage(requestURI,errorEnum.getCode(),errorEnum.getMsg(), ErrorType.CustomerError).toString(),e);
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_BINDB_ZH_FACTOR_ERROR);
        }
        return rtnList;
    }


    public BWJsonResult<MoiraiUser> getBindUserInfo(MoiraiUserMemberCondition condition){
        logger.info("getBindOrgInfo 入参：" + JSONObject.toJSONString(condition));
        List<MoiraiUserMember> moiraiUserMembers = moiraiUserMemberMapper.selectByBean(condition);

        MoiraiUser moiraiUser = null;
        if(moiraiUserMembers.size() ==1){
            MoiraiUserMember moiraiUserMember = moiraiUserMembers.get(0);
            moiraiUser = moiraiUserMapper.selectByPrimaryKey(moiraiUserMember.getUserId());
        }
        return new BWJsonResult(moiraiUser);
    }

    @Transactional
    public void updateUserMember(Map<String,String> map){
        String userId = map.get("userId");
        //设置默认登录的memberId
        String memberId = map.get("memberId");
        //设置默认二次验证的memberId
        String authMemberId = map.get("authMemberId");
        String defaultOrgFlag = map.get("defaultOrgFlag");
        String defaultValidationFlag = map.get("defaultValidationFlag");
        logger.info("updateUserMember userId = 【{}】 memberId = 【{}】 authMemberId = 【{}】 defaultOrgFlag = 【{}】 defaultValidationFlag = 【{}】",
                userId,memberId,authMemberId,defaultOrgFlag,defaultValidationFlag);
        MoiraiUserMemberCondition condition = new MoiraiUserMemberCondition();
        condition.setUserId(Long.valueOf(userId));
        condition.setDefaultOrgFlag(Constants.flag_Y);
        condition.setDefaultValidationFlag(Constants.flag_Y);
        Long nowTime = DateTimeUtils.nowTimeLong();
        try{
            List<MoiraiUserMember> conditions = moiraiUserMemberMapper.selectLoginOrFactor(condition);
            for(MoiraiUserMember member:conditions){
                member.setDefaultOrgFlag(Constants.flag_N);
                member.setDefaultValidationFlag(Constants.flag_N);
                member.setUpdateTime(new Date());
            }
            if(conditions == null || conditions.size() <=0){
                conditions = new ArrayList<>();
            }
            if(!StrUtils.isEmpty(memberId)){
                MoiraiUserMemberCondition logCondition = new MoiraiUserMemberCondition();
                logCondition.setMemberId(Long.valueOf(memberId));
                logCondition.setDefaultOrgFlag(defaultOrgFlag);
                logCondition.setUpdateTime(new Date());
                conditions.add(logCondition);
            }
            if(!StrUtils.isEmpty(authMemberId)){
                MoiraiUserMemberCondition authCondition = new MoiraiUserMemberCondition();
                authCondition.setMemberId(Long.valueOf(authMemberId));
                authCondition.setUpdateTime(new Date());
                if(!StrUtils.isEmpty(defaultValidationFlag)){
                    authCondition.setDefaultValidationFlag(defaultValidationFlag);
                }
                conditions.add(authCondition);
            }
            if(conditions != null && conditions.size()>0){
                moiraiUserMemberMapper.updateMoreMember(conditions);
            }
        }catch (Exception e){
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_SET_DEFAULT_ORG_EXCEPTION;
            logger.error(new ErrorMessage(requestURI,errorEnum.getCode(),errorEnum.getMsg(), ErrorType.CustomerError).toString(),e);
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_BINDB_UPDATE_LOGIN_ERROR);
        }

    }

    /**
     * 解除企业用户绑定
     *
     * @param memberId 绑定id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unBindTenant(Long memberId, Long userId) {
        logger.info("解除企业用户绑定，入参：memberId: {}, userId: {}", memberId, userId);
        MoiraiUserMemberCondition condition = new MoiraiUserMemberCondition();
        condition.setMemberId(memberId);
        condition.setUserId(userId);

        List<MoiraiUserMember> list = moiraiUserMemberMapper.selectByBean(condition);

        if (list.isEmpty()) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_BINDB_NOT_FUND_B_ERROR);
        }
        unBindTenant(list.get(0));
    }

    /**
     * 解除账号绑定，兼容旧接口，后期删除
     *
     * @param cUser
     * @param bUser
     */
    @Override
    public void unBindTenant(MoiraiUser cUser, MoiraiUser bUser) {
        if (cUser == null || bUser == null) {
            logger.info("解除账号绑定失败，用户数据为空，C端用户{}， B端用户{} ",
                    JSONObject.toJSONString(cUser), JSONObject.toJSONString(bUser));
//            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_BINDB_NOT_FUND_B_ERROR);
            return;
        }
        MoiraiUserMemberCondition condition = new MoiraiUserMemberCondition();
        condition.setUserId(cUser.getUserId());
        condition.setBindUserId(bUser.getUserId());
        List<MoiraiUserMember> list = moiraiUserMemberMapper.selectByBean(condition);
        if (list.isEmpty()) {
            logger.info("解除绑定账号失败，无该绑定关系，cUserId:{}, bUserId:{}", cUser.getUserId(), bUser.getUserId());
//            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_BINDB_NOT_FUND_B_ERROR);
            return;
        }
        unBindTenant(list.get(0));
    }

    /**
     * 根据B端用户id解除账号绑定
     * @param bUserId
     */
    @Override
    public void unBindTenant(Long bUserId) {
        MoiraiUserMemberCondition condition = new MoiraiUserMemberCondition();
        condition.setBindUserId(bUserId);
        List<MoiraiUserMember> list = moiraiUserMemberMapper.selectByBean(condition);
        if (!list.isEmpty()){
            unBindTenant(list.get(0));
        }
    }

    /**
     * 解除用户绑定
     * @param member
     */
    private void unBindTenant(MoiraiUserMember member){
        Long cUserId = member.getUserId();
        // 解除绑定
        MoiraiUserMember update = new MoiraiUserMember();
        update.setMemberId(member.getMemberId());
        update.setUpdateUser(cUserId + "");
        update.setDelFlag(Constants.flag_Y);
        Long nowTime = DateTimeUtils.nowTimeLong();
        update.setUpdateTime(new Date());
        moiraiUserMemberMapper.updateByPrimaryKeySelective(update);

        // 更新B端用户
        MoiraiUser moiraiUser = new MoiraiUser();
        moiraiUser.setUserId(member.getBindUserId());
        moiraiUser.setPhoneValidate(Constants.flag_N);
        moiraiUser.setModifyTime(nowTime);
        // 不更新B端用户信息
        moiraiUserMapper.updateByPrimaryKeySelective(moiraiUser);
        // 设置默认绑定信息
//        updateDefaultFlag(member);
    }

    /**
     * 重新设置默认标识
     *
     * @param member 解除的绑定关系
     */
    private void updateDefaultFlag(MoiraiUserMember member) {
        boolean setDefaultOrg = false;
        // 解除绑定的数据，是默认开启双因子标志的数据，随机从还绑定的数据中选一个设置默认双因子
        if (Constants.flag_Y.equals(member.getDefaultValidationFlag())) {
            // 查询绑定了双因子的账号列表
            // 存在数据,取第一个作为默认双因子登陆机构及默认登陆机构
            MoiraiUserMemberCondition syzQuery = new MoiraiUserMemberCondition();
            syzQuery.setUserId(member.getUserId());
            List<MoiraiUserMemberCondition> syzList = getAuthTenant(syzQuery);
            if (syzList != null && !syzList.isEmpty()) {
                Map<String, String> updateMap = new HashMap<>();
                updateMap.put("userId", member.getUserId() + "");
                updateMap.put("authMemberId", syzList.get(0).getMemberId() + "");
                updateMap.put("defaultValidationFlag", Constants.flag_Y);
                updateMap.put("defaultOrgFlag", Constants.flag_N);
                // 解除绑定的数据，是默认组织机构的数据，随机从还绑定的数据中选一个设置默认组织机构
                if (Constants.flag_Y.equals(member.getDefaultOrgFlag())) {
                    updateMap.put("memberId", syzList.get(0).getMemberId() + "");
                    updateMap.put("defaultOrgFlag", Constants.flag_Y);
                    setDefaultOrg = true;
                }
                updateUserMember(updateMap);
            }
        }
        if (Constants.flag_Y.equals(member.getDefaultOrgFlag()) && !setDefaultOrg) {
            // 获取所有已经绑定的信息
            // 取第一个作为默认登陆机构
            MoiraiUserMemberCondition defaultOrgQuery = new MoiraiUserMemberCondition();
            defaultOrgQuery.setUserId(member.getUserId());
            defaultOrgQuery.setPageNo(1);
            defaultOrgQuery.setPageSize(1);
            BWJsonResult<MoiraiUserMemberCondition> defOrgList = getBindTenantInfo(defaultOrgQuery);
            if (defOrgList.getData() != null && !defOrgList.getData().isEmpty()) {
                Map<String, String> updateMap = new HashMap<>();
                updateMap.put("userId", member.getUserId() + "");
                updateMap.put("memberId", defOrgList.getData().get(0).getMemberId() + "");
                updateMap.put("defaultOrgFlag", Constants.flag_Y);
                updateMap.put("defaultValidationFlag", Constants.flag_N);
                updateUserMember(updateMap);
            } else {
                logger.info("解绑后无已绑定企业！不设置默认登陆机构及双因子登陆租户。");
            }
        }
    }

}
