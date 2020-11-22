package com.baiwang.moirai.mapper;

import com.baiwang.moirai.model.tenant.MoiraiChannelTenant;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface MoiraiChannelTenantMapper {

    /**
     * 添加渠道租户关联关系
     **/
    int insertList(List<MoiraiChannelTenant> record);

    /**
     * 根据主键删除
     **/
    int deleteByPrimaryKey(Long channelTenantId);

    /**
     * 根据租户id删除
     **/
    int deleteByTenantId(Long tenantId);

    /**
     * 查询渠道租户关系
     */
    List<MoiraiChannelTenant> queryList(MoiraiChannelTenant record);

    /**
     * 根据租户id列表查询租户渠道关系
     * 运营后台租户查询
     */
    List<MoiraiChannelTenant> selectListByTenantIdList(List<Long> tenantIdList);
}