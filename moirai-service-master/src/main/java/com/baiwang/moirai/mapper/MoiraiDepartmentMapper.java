package com.baiwang.moirai.mapper;

import com.baiwang.moirai.model.department.MoiraiDepartment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MoiraiDepartmentMapper {

    int insertSelective(MoiraiDepartment record);

    MoiraiDepartment selectByPrimaryKey(Long departmentId);

    int updateByPrimaryKeySelective(MoiraiDepartment record);

    List<MoiraiDepartment> selectByPid(Long pId);

    List<MoiraiDepartment> selectByTenantId(Long tenantId);
}