package com.baiwang.moirai.serviceimpl;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.mapper.MoiraiOauth2ClientMapper;
import com.baiwang.moirai.mapper.MoiraiOrgMapper;
import com.baiwang.moirai.mapper.MoiraiUserLoginHistoryMapper;
import com.baiwang.moirai.mapper.MoiraiUserMapper;
import com.baiwang.moirai.model.oauth.MoiraiOauth2Client;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.model.user.MoiraiUserLoginHistoryCondition;
import com.baiwang.moirai.service.MoiraiStatisticService;
import com.baiwang.moirai.service.MoiraiUserMemberService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户中心监控统计
 *
 * @author LC
 * @date 2020/8/7 16:24
 */
@Service
public class MoiraiStatisticServiceImpl implements MoiraiStatisticService {

    @Autowired
    private MoiraiUserLoginHistoryMapper userLoginHistoryMapper;

    @Autowired
    private MoiraiOauth2ClientMapper oauth2ClientMapper;

    @Autowired
    private MoiraiOrgMapper orgMapper;

    @Autowired
    private MoiraiUserMapper userMapper;

    @Override
    public List<Map> queryLogin(MoiraiUserLoginHistoryCondition query) {
        return userLoginHistoryMapper.selectClientCount(query);
    }

    @Override
    public BWJsonResult<MoiraiUserLoginHistoryCondition> queryLoginList(MoiraiUserLoginHistoryCondition query) {
        MoiraiOauth2Client client = null;
        if (StringUtils.isNotBlank(query.getClientId())) {
            client = oauth2ClientMapper.selectByClientId(query.getClientId());
            if (client == null) {
                return new BWJsonResult<>();
            }
        }
        Page info = PageHelper.startPage(query.getPageNo(), query.getPageSize());
        List<MoiraiUserLoginHistoryCondition> hisList = userLoginHistoryMapper.selectLoginList(query);
        for (int i = 0; i < hisList.size(); i++) {
            if (client != null) {
                hisList.get(i).setClientName(client.getClientName());
            }
            if (hisList.get(i).getTenantId() != null) {
                MoiraiOrg queryOrg = new MoiraiOrg();
                queryOrg.setTenantId(hisList.get(i).getTenantId());
                queryOrg.setParentOrg(0L);
                List<MoiraiOrg> orgList = orgMapper.selectMoreOrg(queryOrg);
                if (!orgList.isEmpty()) {
                    hisList.get(i).setTenantName(orgList.get(0).getOrgName());
                    hisList.get(i).setTaxCode(orgList.get(0).getTaxCode());
                }
            }
            MoiraiUser user = userMapper.selectByPrimaryKey(hisList.get(i).getUserId());
            if (user != null) {
                hisList.get(i).setUserName(user.getUserName());
                hisList.get(i).setUserAccount(user.getUserAccount());
                hisList.get(i).setCreateTime(user.getCreateTime());
            }
        }
        return new BWJsonResult<>(hisList, (int) info.getTotal());
    }
}
