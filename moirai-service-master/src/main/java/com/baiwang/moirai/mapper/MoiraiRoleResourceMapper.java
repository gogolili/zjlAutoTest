package com.baiwang.moirai.mapper;

import com.baiwang.moirai.model.role.MoiraiRoleResource;

import java.util.List;

import com.baiwang.moirai.model.role.MoiraiUserAuthz;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

@Service
@Mapper
public interface MoiraiRoleResourceMapper {
    int deleteByPrimaryKey(Long roleResourceId);

    int deleteById(MoiraiRoleResource moiraiRoleResource);

    int insert(MoiraiRoleResource moiraiRoleResource);

    int insertBatchInfo(List<MoiraiRoleResource> listMoiraiRoleResource);

    int insertSelective(MoiraiRoleResource record);

    MoiraiRoleResource selectByPrimaryKey(Long roleResourceId);

    List<MoiraiRoleResource> selectByBean(MoiraiRoleResource moiraiRoleResource);

    int updateByPrimaryKeySelective(MoiraiRoleResource record);

    int updateByPrimaryKey(MoiraiRoleResource record);

    List<Long> selectResourceIds(MoiraiUserAuthz orgRole);

    /**
     * 查询授权角色资源信息
     */
    List<MoiraiRoleResource> selectAuthResource(MoiraiUserAuthz orgRole);

    List<MoiraiRoleResource> selectNoAuthRole(@Param("pid") Long pid, @Param("resourceId") Long resourceId);
}