package com.baiwang.moirai.mapper;

import com.baiwang.moirai.model.role.MoiraiRole;
import com.baiwang.moirai.model.role.MoiraiRoleCondition;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

@Service
@Mapper
public interface MoiraiRoleMapper {

    List<MoiraiRole> selectByBean(MoiraiRole moiraiRole);

    int deleteByPrimaryKey(Long roleId);

    int insert(MoiraiRole record);

    int insertSelective(MoiraiRole record);

    MoiraiRole selectByPrimaryKey(Long roleId);

    int updateByPrimaryKeySelective(MoiraiRole record);

    int updateByPrimaryKey(MoiraiRole record);

    List<MoiraiRole> selectTenantKpyRoles(Map kpyMap);

    List<MoiraiRole> selectAuthRoles(MoiraiRole role);

    /**
     * 查询租户所有角色信息
     */
    List<MoiraiRole> selectAllAuthRoles(MoiraiRole role);

    /**
     * 查询预制产品角色
     *
     * @param list 产品id
     * @return
     */
    List<MoiraiRole> selectProductRoles(List<Long> list);

    List<MoiraiRole> selectRoleBatch(List<Long> roleIds);

    List<MoiraiRole> selectOrgShowRoles(@Param("roleName") String roleName, @Param("tenantId") Long tenantId,
        @Param("products") List<Long> products, @Param("userId") Long userId, @Param("resourceId") Long resourceId);
}