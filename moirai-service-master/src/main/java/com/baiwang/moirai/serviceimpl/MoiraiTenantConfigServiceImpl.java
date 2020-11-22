package com.baiwang.moirai.serviceimpl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.common.Constants;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.mapper.MoiraiTenantConfigMapper;
import com.baiwang.moirai.model.sys.SysDict;
import com.baiwang.moirai.model.tenant.MoiraiTenant;
import com.baiwang.moirai.model.tenant.MoiraiTenantConfig;
import com.baiwang.moirai.model.tenant.MoiraiTenantConfigCondition;
import com.baiwang.moirai.service.MoiraiTenantConfigService;
import com.baiwang.moirai.utils.RedisUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 租户配置
 *
 * @author LC
 * @date 2019/12/9 14:14
 */
@Service
public class MoiraiTenantConfigServiceImpl implements MoiraiTenantConfigService {

    @Autowired
    private MoiraiTenantConfigMapper moiraiTenantConfigMapper;

    @Autowired
    private RedisUtils redisUtils;

    /**
     * 获取配置列表
     *
     * @param condition
     * @return
     */
    @Override
    public BWJsonResult<MoiraiTenantConfig> getConfigList(MoiraiTenantConfigCondition condition) {
        if (condition.getTenantId() == null) {
            throw new MoiraiException("-1", "租户id必填！");
        }
        Page page = PageHelper.startPage(condition.getPageNo(), condition.getPageSize());
        List<MoiraiTenantConfig> list = moiraiTenantConfigMapper.selectConfigList(condition);
        return new BWJsonResult<>(list, (int) page.getTotal());
    }

    /**
     * 查询租户配置列表 缓存
     *
     * @param tenantId
     * @return
     */
    @Override
    public List<MoiraiTenantConfig> getConfigListCache(Long tenantId, String configCode) {
        List<MoiraiTenantConfig> list = redisUtils.getList(Constants.MOIRAI_TENANT_CONFIG + tenantId, MoiraiTenantConfig.class);
        if (list == null) {
            MoiraiTenantConfigCondition condition = new MoiraiTenantConfigCondition();
            condition.setTenantId(tenantId);
            list = moiraiTenantConfigMapper.selectConfigList(condition);
            redisUtils.set(Constants.MOIRAI_TENANT_CONFIG + tenantId, list, 60 * 60 * 6);
        }
        if (StringUtils.isNotBlank(configCode)) {
            List<MoiraiTenantConfig> result = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getConfigCode().equals(configCode)) {
                    result.add(list.get(i));
                }
            }
            return result;
        }
        return list;
    }

    /**
     * 清除租户配置 缓存 信息
     *
     * @param tenantId
     */
    @Override
    public void clearConfigListCache(Long tenantId) {
        if (tenantId == null) {
            redisUtils.delete(redisUtils.keys(Constants.MOIRAI_TENANT_CONFIG + "*"));
        } else {
            redisUtils.delete(Constants.MOIRAI_TENANT_CONFIG + tenantId);
        }
    }

    /**
     * 添加配置
     *
     * @param moiraiTenant
     */
    @Override
    public void addConfig(MoiraiTenant moiraiTenant) {

    }

    /**
     * 更新配置
     *
     * @param moiraiTenant
     */
    @Override
    public void updateConfig(MoiraiTenant moiraiTenant) {

    }

    /**
     * 删除配置
     *
     * @param id
     */
    @Override
    public void delConfig(Long id) {

    }
}
