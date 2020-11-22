package com.baiwang.moirai.controller.user;

import com.alibaba.fastjson.JSONObject;
import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.api.MoiraiUserOtherSvc;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.model.role.MoiraiResource;
import com.baiwang.moirai.model.scale.MoiraiUserDataScope;
import com.baiwang.moirai.model.user.BWToken;
import com.baiwang.moirai.model.user.MoiraiSysUsers;
import com.baiwang.moirai.model.user.MoiraiSysUsersExtra;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.service.MoiraiSysUsersService;
import com.baiwang.moirai.service.MoiraiUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Auther: liuzhenyun
 * @Date: 2019-06-20 13:37
 * @Description:
 */
@RestController
public class MoiraiUserOtherController implements MoiraiUserOtherSvc {


    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MoiraiUserService moiraiUserService;

    @Resource
    private MoiraiSysUsersService moiraiSysUsersService;

    /**
     * 通过用户id查询用户数据范围
     * @param scope
     * @return
     */
    @Override
    public BWJsonResult<MoiraiUserDataScope> getUserScpoe(@RequestBody MoiraiUserDataScope scope){
        if(scope.getUserId()==null){
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        logger.info("通过用户id：【{}】查询用户数据范围",scope.getUserId());
        return new BWJsonResult(moiraiUserService.getScopeByUser(scope));
    }

    /**
     * 通过用户id、机构id查询授权的ClickPaaS资源
     * @param user
     * @return
     */
    @Override
    public BWJsonResult<MoiraiResource> getUserCPResource(@RequestBody MoiraiUser user){
        if(user.getUserId()==null || user.getLoginOrg()==null){
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        logger.info("通过用户id：【{}】机构id:【{}】查询授权的ClickPaaS资源",user.getUserId(),user.getLoginOrg());
        return new BWJsonResult(moiraiUserService.getCPResource(user));
    }

    @Override
    public BWJsonResult syncUsers(@RequestBody MoiraiUser moiraiUser) {
        if( moiraiUser.getTenantId()==null){
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        logger.info("开始同步租户id：【{}】下开通CP功能的用户",moiraiUser.getTenantId());
        moiraiUserService.getUsersByTenantId(moiraiUser.getTenantId());
        return new BWJsonResult();
    }

    /**
     * 第三方用户兑换百望用户
     * @param moiraiSysUsers
     * @return
     */
    @Override
    public BWJsonResult changeBwUser(@RequestBody MoiraiSysUsers moiraiSysUsers){
        logger.info("兑换百望用户，入参：【{}】", moiraiSysUsers);
        MoiraiUser user = moiraiSysUsersService.getBwUser(moiraiSysUsers);
        logger.info("兑换百望用户，入参：【{}】,结果：【{}】", moiraiSysUsers, user);
        return new BWJsonResult<>(user);
    }

    /**
     * 根据第三方登陆用户信息获取百望用户信息
     *
     * @param ssoUserInfo
     * @return
     */
    @PostMapping("/getUserBySsoUserInfo")
    public BWJsonResult<MoiraiUser> getUserBySsoUserInfo(@RequestBody BWToken ssoUserInfo) {
        logger.info("根据第三方登陆用户信息获取百望用户信息！ 入参： {}", JSONObject.toJSONString(ssoUserInfo));
        MoiraiUser user = moiraiUserService.getUserBySsoUserInfo(ssoUserInfo);
        return BWJsonResult.success(user);
    }

    /**
     * 绑定第三方用户信息
     *
     * @param user
     * @return
     */
    @PostMapping("/bindSsoUser")
    public BWJsonResult<MoiraiUser> bindSsoUser(@RequestBody MoiraiUser user) {
        logger.info("绑定第三方用户信息！");
        user = moiraiUserService.bindSsoUser(user);
        return BWJsonResult.success(user);
    }

    /**
     * 第三方用户绑定百望用户
     * @param moiraiSysUsersExtra
     * @return
     */
    @Override
    public BWJsonResult bindBwUser(@RequestBody MoiraiSysUsersExtra moiraiSysUsersExtra){
        logger.info("绑定百望用户，入参：【{}】", moiraiSysUsersExtra);
        BWJsonResult result = moiraiSysUsersService.bindBwUser(moiraiSysUsersExtra);
        logger.info("绑定百望用户，入参：【{}】,结果：【{}】", moiraiSysUsersExtra, result);
        return result;
    }


}
