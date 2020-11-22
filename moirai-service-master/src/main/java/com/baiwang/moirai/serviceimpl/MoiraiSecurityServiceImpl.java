package com.baiwang.moirai.serviceimpl;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.cloud.common.util.StringUtil;
import com.baiwang.moirai.common.Constants;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.mapper.MoiraiModuleMapper;
import com.baiwang.moirai.mapper.MoiraiResourceMapper;
import com.baiwang.moirai.mapper.MoiraiSecurityControlMapper;
import com.baiwang.moirai.model.role.MoiraiModuleService;
import com.baiwang.moirai.model.role.MoiraiModuleServiceCondition;
import com.baiwang.moirai.model.role.MoiraiResource;
import com.baiwang.moirai.model.role.MoiraiResourceSecurity;
import com.baiwang.moirai.model.role.MoiraiResourceSecurityCondition;
import com.baiwang.moirai.model.role.MoiraiSecurityControl;
import com.baiwang.moirai.model.role.MoiraiSecurityControlCondition;
import com.baiwang.moirai.service.MoiraiSecurityService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author LC
 */
@Service
public class MoiraiSecurityServiceImpl implements MoiraiSecurityService {

    @Autowired
    private MoiraiSecurityControlMapper moiraiSecurityControlMapper;

    @Autowired
    private MoiraiModuleMapper moiraiModuleMapper;

    @Autowired
    private MoiraiResourceMapper moiraiResourceMapper;

    @Autowired
    @Qualifier("redisTrafficTemplate")
    private RedisTemplate redisTemplate;

    /**
     * 查询规则列表
     *
     * @param condition
     * @return
     */
    @Override
    public BWJsonResult<MoiraiSecurityControl> getSecurityControlList(MoiraiSecurityControlCondition condition) {
        if (0 >= condition.getPageNo()) {
            condition.setPageNo(1);
        }
        if (0 >= condition.getPageSize()) {
            condition.setPageSize(10);
        }
        PageHelper.startPage(condition.getPageNo(), condition.getPageSize());
        List<MoiraiSecurityControl> result = moiraiSecurityControlMapper.selectListByCondition(condition);
        PageInfo<MoiraiSecurityControl> pageInfo = new PageInfo<>(result);
        return new BWJsonResult<>(result, (int) pageInfo.getTotal());
    }

    /**
     * 根据资源id查询已启用规则列表
     *
     * @param condition
     * @return
     */
    @Override
    public BWJsonResult<MoiraiSecurityControl> getResourceSecurityList(MoiraiResourceSecurityCondition condition) {
        if (0 >= condition.getPageNo()) {
            condition.setPageNo(1);
        }
        if (0 >= condition.getPageSize()) {
            condition.setPageSize(10);
        }
        PageHelper.startPage(condition.getPageNo(), condition.getPageSize());
        List<MoiraiSecurityControl> result = moiraiSecurityControlMapper.getResourceSecurityList(condition);
        PageInfo<MoiraiSecurityControl> pageInfo = new PageInfo<>(result);
        return new BWJsonResult<>(result, (int) pageInfo.getTotal());
    }

    /**
     * 添加规则
     */
    @Override
    public void addSecurityControl(MoiraiSecurityControl control) {
        MoiraiSecurityControl exList = moiraiSecurityControlMapper.selectByName(control.getName());
        if (exList != null) {
            throw new MoiraiException("-1", "规则名称已存在");
        }
        moiraiSecurityControlMapper.insertSelective(control);
    }

    /**
     * 修改规则
     */
    @Override
    public void updateSecurityControl(MoiraiSecurityControl control) {
        if (!StringUtil.isBlank(control.getName())) {
            MoiraiSecurityControl selectByName = moiraiSecurityControlMapper.selectByName(control.getName());
            if (selectByName != null && !selectByName.getId().equals(control.getId())) {
                throw new MoiraiException("-1", "规则名称已存在");
            }
        }
        moiraiSecurityControlMapper.updateByPrimaryKeySelective(control);
    }

    /**
     * 删除规则
     *
     * @param control
     */
    @Override
    public void delSecurityControl(MoiraiSecurityControl control) {
        MoiraiResourceSecurityCondition query = new MoiraiResourceSecurityCondition();
        query.setSecurityControlId(control.getId());
        List<MoiraiResourceSecurity> exList = moiraiSecurityControlMapper.selectResourceSecurity(query);
        if (exList.size() > 0) {
            throw new MoiraiException("-1", "该规则已授权资源，请先解除资源授权！");
        }
        moiraiSecurityControlMapper.deleteByPrimaryKey(control.getId());
    }

    /**
     * 绑定资源规则
     *
     * @param security
     */
    @Override
    @Transactional
    public void insertResourceSecurity(List<MoiraiResourceSecurity> security) {
        for (MoiraiResourceSecurity resourceSecurity : security) {
            if (resourceSecurity.getSecurityControlId() == null || resourceSecurity.getResourceId() == null || resourceSecurity.getResourceType() == null) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
            }
            moiraiSecurityControlMapper.insertResourceSecurity(resourceSecurity);
        }
        resetJudgeAuthc(security.get(0));
    }

    private void resetJudgeAuthc(MoiraiResourceSecurity security) {
        String judgeAuthc = null;
        if (Constants.DEFAULT_ONE.equals(security.getResourceType())) {
            MoiraiResource moiraiResource = moiraiResourceMapper.selectByPrimaryKey(security.getResourceId());
            if (moiraiResource != null && StringUtil.isNotEmpty(moiraiResource.getJudgeAuthc())) {
                judgeAuthc = moiraiResource.getJudgeAuthc();
            }
        } else {
            MoiraiModuleService moduleService = moiraiModuleMapper.selectByPrimaryKey(security.getResourceId());
            if (moduleService != null && StringUtil.isNotEmpty(moduleService.getUrlPrefix())) {
                judgeAuthc = moduleService.getUrlPrefix();
            }
        }
        if (StringUtil.isNotEmpty(judgeAuthc)) {
            MoiraiResourceSecurityCondition securityCondition = new MoiraiResourceSecurityCondition();
            securityCondition.setResourceId(security.getResourceId());
            securityCondition.setResourceType(security.getResourceType());
            List<MoiraiSecurityControl> securityList = moiraiSecurityControlMapper.getResourceSecurityList(securityCondition);
            redisTemplate.opsForValue().set(Constants.JUDGE_PERMISSION + judgeAuthc, securityList.toString());
        } else {
            redisTemplate.delete(Constants.JUDGE_PERMISSION + judgeAuthc);
        }
    }

    /**
     * 解绑资源规则
     */
    @Override
    public void deleteResourceSecurity(MoiraiResourceSecurity security) {
        int i = moiraiSecurityControlMapper.deleteResourceSecurity(security);
        resetJudgeAuthc(security);
    }

    @Override
    public BWJsonResult<MoiraiSecurityControl> queryUnbindSecurity(MoiraiResourceSecurityCondition securityCondition) {
        if (0 >= securityCondition.getPageNo()) {
            securityCondition.setPageNo(1);
        }
        if (0 >= securityCondition.getPageSize()) {
            securityCondition.setPageSize(10);
        }
        PageHelper.startPage(securityCondition.getPageNo(), securityCondition.getPageSize());
        List<MoiraiSecurityControl> result = moiraiSecurityControlMapper.queryUnbindSecurity(securityCondition);
        PageInfo<MoiraiSecurityControl> pageInfo = new PageInfo<>(result);
        return new BWJsonResult<>(result, (int) pageInfo.getTotal());
    }

    // 通用规则列表

    @Override
    public BWJsonResult<MoiraiModuleService> queryGeneralInfo(MoiraiModuleServiceCondition serviceCondition) {
        if (0 >= serviceCondition.getPageNo()) {
            serviceCondition.setPageNo(1);
        }
        if (0 >= serviceCondition.getPageSize()) {
            serviceCondition.setPageSize(10);
        }
        PageHelper.startPage(serviceCondition.getPageNo(), serviceCondition.getPageSize());
        List<MoiraiModuleService> controls = null;
        PageInfo<MoiraiModuleService> pageInfo = null;
        try {
            controls = moiraiModuleMapper.selectListByCondition(serviceCondition);
            pageInfo = new PageInfo<>(controls);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new BWJsonResult<>(controls, (int) pageInfo.getTotal());
    }

    @Override
    public int addGeneralInfo(MoiraiModuleService generalInfo) {
        MoiraiModuleServiceCondition condition = new MoiraiModuleServiceCondition();
        condition.setServiceName(generalInfo.getServiceName());
        condition.setUrlPrefix(generalInfo.getUrlPrefix());
        List<MoiraiModuleService> services = moiraiModuleMapper.selectListByCondition(condition);
        if (services.isEmpty()) {
            return moiraiModuleMapper.insertSelective(generalInfo);
        } else {
            throw new MoiraiException("-1", "信息已存在");
        }

    }

    @Override
    public int editGeneralInfo(MoiraiModuleService queryGeneralInfo) {
        MoiraiModuleServiceCondition condition = new MoiraiModuleServiceCondition();
        condition.setServiceName(queryGeneralInfo.getServiceName());
        condition.setUrlPrefix(queryGeneralInfo.getUrlPrefix());
        List<MoiraiModuleService> services = moiraiModuleMapper.selectListByCondition(condition);
        if (services.size() > 0 && !services.get(0).getId().equals(queryGeneralInfo.getId())) {
            throw new MoiraiException("-1", "信息已存在");
        } else {
            return moiraiModuleMapper.updateByPrimaryKeySelective(queryGeneralInfo);
        }
    }

    @Override
    public int deleteGeneralInfo(MoiraiModuleService moiraiModuleService) {
        MoiraiModuleService moduleService = moiraiModuleMapper.selectByPrimaryKey(moiraiModuleService.getId());
        int i = 0;
        if (moduleService != null) {
            i = moiraiModuleMapper.deleteByPrimaryKey(moiraiModuleService.getId());
            redisTemplate.delete(Constants.JUDGE_PERMISSION + moduleService.getUrlPrefix());
        }
        return i;
    }
}
