package com.baiwang.moirai.mapper;

import com.baiwang.moirai.model.department.MoiraiDepartment;
import com.baiwang.moirai.model.department.MoiraiDepartmentUser;
import com.baiwang.moirai.model.department.MoiraiDepartmentUserView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MoiraiDepartmentUserMapper {

    int deleteByDepartmentId(Long id);

    int insertSelective(MoiraiDepartmentUser record);

    MoiraiDepartmentUser selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(MoiraiDepartmentUser record);

    /**
     * 通过部门id和用户id 删除部门用户关系
     * @param userId
     * @param departmentId
     * @return
     */
    int deleteByUserIdAndDepartmentId(@Param("userId") Long userId, @Param("departmentId") Long departmentId);

    /**
     * 批量删除部门与用户的关系
     * @param list
     * @return
     */
    int deleteBatch(List<MoiraiDepartmentUser> list);

    /**
     * 批量添加部门与用户的关系
     * @param list
     * @return
     */
    int insertBatch(List<MoiraiDepartmentUser> list);

    /**
     * 查询部门已添加的成员
     * @param department
     * @return
     */
    List<MoiraiDepartmentUserView> findDepartmentUser(MoiraiDepartment department);

}