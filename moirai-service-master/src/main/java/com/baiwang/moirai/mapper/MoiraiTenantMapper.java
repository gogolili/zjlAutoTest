package com.baiwang.moirai.mapper;

import com.baiwang.moirai.model.tenant.MoiraiTenant;
import com.baiwang.moirai.model.tenant.MoiraiTenantListCondition;
import com.baiwang.moirai.model.tenant.MoiraiTenantListVO;
import com.baiwang.moirai.model.tenant.MoiraiTenantVO;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface MoiraiTenantMapper {

    List<MoiraiTenant> batchQueryByIds(List tenantIds);

    /** 前端添加租户 **/
    int insertTenant(MoiraiTenantVO moiraiTenant);

    /** 更新完善租户 **/
    int updateTenant(MoiraiTenant moiraiTenant);

    int insert(MoiraiTenant record);

    int insertSelective(MoiraiTenant record);

    MoiraiTenant selectByPrimaryKey(Long tenantId);

    /** 查询租户列表 **/
    List<MoiraiTenantListVO> queryTenantListPage(MoiraiTenantListCondition tenantListCondition);

    /**
     * 查询租户列表 单表查询 运营后台租户查询
     */
    List<MoiraiTenantListVO> queryTenantList2Page(MoiraiTenantListCondition tenantListCondition);

    /** 按照条件查询租户 **/
    List<MoiraiTenantVO> queryTenantByCondition(MoiraiTenantVO moiraiTenantVO);

    int addTenantList(List<MoiraiTenant> moiraiTenantList);

    int deleteTenantByTenantId(Long tenantId);

    int deleteUserByTenantId(Long tenantId);

    int deleteOrgByTenantId(Long tenantId);

    int deleteOrgProductByTenantId(Long tenantId);

    int deleteUserAuthByExample(Long tenantId);

    int deleteRoleByTenantId(Long tenantId);

    int deleteRoleResourceByTenantId(Long tenantId);

    int deleteOrgConfigByTenantId(Long tenantId);

    int deleteUserMember(Long tenantId);

    @MapKey("tenantId")
    Map<Long, MoiraiTenant> selectEmail(List<Long> list);
}