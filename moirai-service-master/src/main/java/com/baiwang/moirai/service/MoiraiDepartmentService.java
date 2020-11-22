package com.baiwang.moirai.service;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.model.department.MoiraiDepartment;
import com.baiwang.moirai.model.department.MoiraiDepartmentUser;
import com.baiwang.moirai.model.department.MoiraiDepartmentUserAddModel;
import com.baiwang.moirai.model.department.MoiraiDepartmentUserView;
import com.baiwang.moirai.model.user.MoiraiUser;

import java.util.List;

/**
 * @Auther: liuzhenyun
 * @Date: 2019-04-12 15:42
 * @Description:
 */
public interface MoiraiDepartmentService {

    /**
     * 添加默认部门
     * @param moiraiDepartment
     * @return
     */
    MoiraiDepartment addDefault(MoiraiDepartment moiraiDepartment);

    /**
     * 添加指定部门的子部门
     * @param moiraiDepartment
     * @return
     */
    MoiraiDepartment add(MoiraiDepartment moiraiDepartment);

    /**
     * 修改指定部门
     * @param moiraiDepartment
     * @return
     */
    void update(MoiraiDepartment moiraiDepartment);

    /**
     * 删除指定部门
     * @param moiraiDepartment
     * @return
     */
    void delete(MoiraiDepartment moiraiDepartment);

    /**
     * 查询租户下所有的部门列表形式
     * @param moiraiDepartment
     * @return
     */

    List<MoiraiDepartment> findDepartmentsList(MoiraiDepartment moiraiDepartment);

    /**
     * 查询租户下所有的部门 树形形式
     * @param moiraiDepartment
     * @return
     */

    List<MoiraiDepartment> findDepartmentsTree(MoiraiDepartment moiraiDepartment);

    /**
     * 删除部门成员
     * @param list
     * @return
     */
    void deleteDepartmentUser(List<MoiraiDepartmentUser> list);

    /**
     * 查询可以添加的人员
     * @param moiraiDepartment 租户id、部门id
     * @return
     */
    List<MoiraiUser> findUserWaitAdd(MoiraiDepartment moiraiDepartment);

    /**
     * 批量添加成员
     * @param model 成员
     * @return
     */
    BWJsonResult addUsers2Department(MoiraiDepartmentUserAddModel model);

    /**
     * 查询指定部门已添加的用户列表
     * @param department
     * @return
     */
    List<MoiraiDepartmentUserView> findDepartmentUser(MoiraiDepartment department);
}
