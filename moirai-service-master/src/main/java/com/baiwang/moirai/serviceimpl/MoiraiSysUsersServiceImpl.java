package com.baiwang.moirai.serviceimpl;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.common.Constants;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.feignclient.SeqnumFeignClient;
import com.baiwang.moirai.mapper.MoiraiSysUsersMapper;
import com.baiwang.moirai.mapper.MoiraiUserMapper;
import com.baiwang.moirai.mapper.MoiraiUserMemberMapper;
import com.baiwang.moirai.model.user.*;
import com.baiwang.moirai.service.MoiraiSysUsersService;
import com.baiwang.moirai.utils.AdminUtils;
import com.baiwang.moirai.utils.DateTimeUtils;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service("moiraiSysUsersService")
public class MoiraiSysUsersServiceImpl implements MoiraiSysUsersService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private MoiraiSysUsersMapper moiraiSysUsersMapper;

    @Resource
    private MoiraiUserMapper moiraiUserMapper;

    @Autowired
    private SeqnumFeignClient seqnumFeignClient;

    @Autowired
    private MoiraiUserMemberMapper moiraiUserMemberMapper;



    @Override
    public MoiraiUser getBwUser(MoiraiSysUsers moiraiSysUsers) {
        //1、通过第三方用户表兑换
        MoiraiUser moiraiUser = moiraiSysUsersMapper.getBwUser(moiraiSysUsers.getSysUserId());
        if (null == moiraiUser) {
            //2、查询用户表
            moiraiUser = moiraiUserMapper.getUserByUserAccount(moiraiSysUsers.getUserAccount());
        }
        return moiraiUser;
    }

    /**
     * 为第三方用户绑定百望用户
     *
     * @param moiraiSysUsersExtra 1)	校验租户ID，确定用户属于此租户 2)	校验用户是否已经被绑定 3)	校验用户名/密码 4)	调用校验用户状态方法
     */
    @Override
    public BWJsonResult bindBwUser(MoiraiSysUsersExtra moiraiSysUsersExtra) {
        //校验用户是否已经被绑定
        MoiraiSysUsers sysUsers = moiraiSysUsersMapper.selectByUserAccount(moiraiSysUsersExtra.getUserAccount());
        if (sysUsers != null) {
            return new BWJsonResult(new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_BINDED));
        }

        //通过用户名密码查询用户
        MoiraiUser user = moiraiUserMapper.getUserByUserAccount(moiraiSysUsersExtra.getUserAccount());
        if (null == user) {
            //用户不存在
            return new BWJsonResult(new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_PASSWORD_MISS_ERROR));
        } else {
            String yhMm = AdminUtils.getUuidPasswd(moiraiSysUsersExtra.getUserPassword(), user.getUuid());
            if (!yhMm.equals(user.getUserPassword())) {
                //密码错误
                return new BWJsonResult(new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_PASSWORD_MISS_ERROR));
            }
            if (!user.getTenantId().equals(moiraiSysUsersExtra.getTenantId())) {
                //租户id不一致，不能绑定其他租户的用户
                return new BWJsonResult(new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_NOT_BINDED_TEANATID));
            }
        }

        MoiraiSysUsers moiraiSysUsers = new MoiraiSysUsers();
        //进行绑定
        try {
            moiraiSysUsers.setUserAccount(moiraiSysUsersExtra.getUserAccount());
            moiraiSysUsers.setSysUserId(moiraiSysUsersExtra.getSysUserId());
            moiraiSysUsers.setCreater(moiraiSysUsersExtra.getCreater());
            moiraiSysUsers.setCreateTime(Long.valueOf(DateTimeUtils.nowMinuteString()));
            moiraiSysUsers.setTenantId(moiraiSysUsersExtra.getTenantId());
            moiraiSysUsers.setSysCode(moiraiSysUsersExtra.getSysCode());
            moiraiSysUsers.setId(seqnumFeignClient.getNum(Constants.MOIRAI_USER_AUTHZ));
            moiraiSysUsersMapper.insertSelective(moiraiSysUsers);
        } catch (Exception e) {
            //绑定错误
            return new BWJsonResult(new MoiraiException(MoiraiErrorEnum.Moirai_DB_ERROR));
        }

        //用户状态
        if (user.getUseFlag().equals("N")) {
            return new BWJsonResult(new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_UNUSERD));
        } else if (user.getUseFlag().equals("L")) {
            return new BWJsonResult(new MoiraiException(MoiraiErrorEnum.MOIRAI_USER_LOCKED));
        } else {
            //绑定成功
            return new BWJsonResult(moiraiSysUsers);
        }

    }


}
