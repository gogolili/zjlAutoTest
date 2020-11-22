package com.baiwang.moirai.mapper;

import com.baiwang.moirai.model.tenant.MoiraiTenantConfig;
import com.baiwang.moirai.model.tenant.MoiraiTenantConfigCondition;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 版权：百望股份有限公司
 * <p>
 * MoiraiTenantConfigMapper
 *
 * @author LC
 * @date 2019-12-09 10:26:13
 */
@Mapper
@Component
public interface MoiraiTenantConfigMapper {
    /**
     * 根据主键删除数据
     *
     * @param id Long
     * @return int
     */
    int deleteByPrimaryKey(Long id);

    /**
     * 插入数据库记录（建议使用）
     *
     * @param record MoiraiTenantConfig
     * @return int
     */
    int insertSelective(MoiraiTenantConfig record);

    /**
     * 根据主键id查询
     *
     * @param id Long
     * @return MoiraiTenantConfig
     */
    MoiraiTenantConfig selectByPrimaryKey(Long id);

    /**
     * 修改数据(推荐使用)
     *
     * @param record MoiraiTenantConfig
     * @return int
     */
    int updateByPrimaryKeySelective(MoiraiTenantConfig record);

    /**
     * 查询租户配置列表
     *
     * @param condition
     */
    List<MoiraiTenantConfig> selectConfigList(MoiraiTenantConfigCondition condition);
}