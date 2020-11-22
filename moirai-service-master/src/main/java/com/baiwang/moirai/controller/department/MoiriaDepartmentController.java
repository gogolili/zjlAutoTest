package com.baiwang.moirai.controller.department;

import com.baiwang.moirai.api.MoiraiDepartmentSvc;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.model.department.MoiraiDepartment;
import com.baiwang.moirai.model.department.MoiraiDepartmentUser;
import com.baiwang.moirai.model.department.MoiraiDepartmentUserAddModel;
import com.baiwang.moirai.model.department.MoiraiDepartmentUserView;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.service.MoiraiDepartmentService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Auther: liuzhenyun
 * @Date: 2019-04-12 15:40
 * @Description:部门管理控制器
 */
@RestController
public class MoiriaDepartmentController implements MoiraiDepartmentSvc {

    private Logger logger = LoggerFactory.getLogger(MoiriaDepartmentController.class);

    @Autowired
    private MoiraiDepartmentService moiraiDepartmentService;

    /**
     * 添加默认部门
     * 场景一：添加租户时，默认调用
     * 场景二：后台手动调用，用于维护历史数据
     *
     * @param moiraiDepartment 租户id
     * @return
     */
    @Override
    public BWJsonResult<MoiraiDepartment> addDefaultDepartment(@RequestBody MoiraiDepartment moiraiDepartment) {
        if (null == moiraiDepartment || null == moiraiDepartment.getTenantId()) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        return new BWJsonResult<>(moiraiDepartmentService.addDefault(moiraiDepartment));
    }

    /**
     * 添加子部门
     * 场景一：添加指定部门的子部门
     *
     * @param moiraiDepartment 上级部门的id
     * @return
     */
    @Override
    public BWJsonResult<MoiraiDepartment> addDepartment(@RequestBody MoiraiDepartment moiraiDepartment) {
        if (null == moiraiDepartment || null == moiraiDepartment.getpId()) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        return new BWJsonResult<>(moiraiDepartmentService.add(moiraiDepartment));
    }

    /**
     * 修改部门
     * 场景一：修改指定部门
     *
     * @param moiraiDepartment 部门id
     * @return
     */
    @Override
    public BWJsonResult updateDepartment(@RequestBody MoiraiDepartment moiraiDepartment) {
        if (null == moiraiDepartment || null == moiraiDepartment.getDepartmentId()) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        moiraiDepartmentService.update(moiraiDepartment);
        return new BWJsonResult();
    }

    /**
     * 删除部门
     * 场景一：删除指定部门：只能删除末级部门，存在子部门不可以删除
     *
     * @param moiraiDepartment
     * @return
     */
    @Override
    public BWJsonResult deleteDepartment(@RequestBody MoiraiDepartment moiraiDepartment) {
        if (null == moiraiDepartment || null == moiraiDepartment.getDepartmentId()) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        moiraiDepartmentService.delete(moiraiDepartment);
        return new BWJsonResult();
    }

    /**
     * 查询部门 树形
     * 场景一：查询指定租户下的部门列表，树形结构展示
     *
     * @param moiraiDepartment
     * @return
     */
    @Override
    public BWJsonResult<MoiraiDepartment> findDepartmentsTree(@RequestBody MoiraiDepartment moiraiDepartment) {
        if (null == moiraiDepartment || null == moiraiDepartment.getTenantId()) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        return new BWJsonResult<>(moiraiDepartmentService.findDepartmentsTree(moiraiDepartment));
    }

    /**
     * 查询部门 可添加的成员列表
     * 场景一：查询指定租户 可以添加的成员列表
     *
     * @param moiraiDepartment
     * @return TODO:用户职位？
     */
    @Override
    public BWJsonResult<MoiraiUser> findUserWaitAdd(@RequestBody MoiraiDepartment moiraiDepartment) {
        if (null == moiraiDepartment || null == moiraiDepartment.getTenantId() || null == moiraiDepartment.getDepartmentId()) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        return new BWJsonResult<>(moiraiDepartmentService.findUserWaitAdd(moiraiDepartment));
    }


    /**
     * 添加成员(支持批量操作)
     * 场景一：添加部门的主管
     * 场景二：添加部门普通员工
     *
     * @param model
     * @return
     */
    @Override
    public BWJsonResult<MoiraiDepartmentUser> addUsers2Department(@RequestBody MoiraiDepartmentUserAddModel model) {
        if (null == model || null == model.getDepartmentId() || null == model.getTenantId()) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        return moiraiDepartmentService.addUsers2Department(model);
    }

    /**
     * 查询部门 已添加的成员列表
     * 场景一：查询指定部门 已添加的成员列表
     *
     * @param moiraiDepartment
     * @return
     */
    @Override
    public BWJsonResult<MoiraiDepartmentUserView> findDepartmentUser(@RequestBody MoiraiDepartment moiraiDepartment) {
        if (null == moiraiDepartment || null == moiraiDepartment.getDepartmentId()) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        return new BWJsonResult<>(moiraiDepartmentService.findDepartmentUser(moiraiDepartment));
    }

    /**
     * 删除成员（支持批量）
     * 场景一：删除指定租户下的成员
     *
     * @param list
     * @return
     */
    @Override
    public BWJsonResult deleteDepartmentUser(@RequestBody List<MoiraiDepartmentUser> list) {
        if (list.isEmpty()) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        moiraiDepartmentService.deleteDepartmentUser(list);
        return new BWJsonResult();
    }

    /**
     * 部门列表 （列表形式）
     * 场景一：添加/修改用户信息时，查询部门列表，即用户所在租户下的部门列表
     *
     * @param moiraiDepartment
     * @return
     */
    @Override
    public BWJsonResult<MoiraiDepartment> findDepartmentsList(@RequestBody MoiraiDepartment moiraiDepartment) {
        if (null == moiraiDepartment || null == moiraiDepartment.getTenantId()) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        return new BWJsonResult<>(moiraiDepartmentService.findDepartmentsList(moiraiDepartment));
    }
}
