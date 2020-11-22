/*
 * @项目名称: Moirai
 * @文件名称: MoiraiTenantZomMapper.java
 * @Date: 17-11-4 下午3:57
 * @author Lance cui
 *
 */

package com.baiwang.moirai.mapper;

import com.baiwang.moirai.model.tenant.MoiraiTenant;
import com.baiwang.moirai.model.tenant.MoiraiTenantListCondition;
import com.baiwang.moirai.model.tenant.MoiraiTenantListVO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@Mapper
public interface MoiraiTenantZomMapper {

    /**根据租户名称查询租户**/
    MoiraiTenant selectByTenantName(String tenantName);

    int deleteByPrimaryKey(Long tenantId);

    int insert(MoiraiTenant record);

    int insertSelective(MoiraiTenant record);

    MoiraiTenant selectByPrimaryKey(Long tenantId);

    int updateByPrimaryKeySelective(MoiraiTenant record);

    int updateByPrimaryKey(MoiraiTenant record);

    List<MoiraiTenantListVO> queryTenantListPage(MoiraiTenantListCondition tenantListCondition);

    /**逻辑删除**/
    void deleteLogic(Long tenantId);
}