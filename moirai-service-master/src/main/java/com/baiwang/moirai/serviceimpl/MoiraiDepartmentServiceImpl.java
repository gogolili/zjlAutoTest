package com.baiwang.moirai.serviceimpl;

import com.alibaba.fastjson.JSONObject;
import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.common.Constants;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.feignclient.SeqnumFeignClient;
import com.baiwang.moirai.mapper.MoiraiDepartmentMapper;
import com.baiwang.moirai.mapper.MoiraiDepartmentUserMapper;
import com.baiwang.moirai.mapper.MoiraiTenantMapper;
import com.baiwang.moirai.mapper.MoiraiUserMapper;
import com.baiwang.moirai.model.department.MoiraiDepartment;
import com.baiwang.moirai.model.department.MoiraiDepartmentUser;
import com.baiwang.moirai.model.department.MoiraiDepartmentUserAddModel;
import com.baiwang.moirai.model.department.MoiraiDepartmentUserView;
import com.baiwang.moirai.model.tenant.MoiraiTenant;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.service.MoiraiDepartmentService;
import com.baiwang.moirai.utils.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: liuzhenyun
 * @Date: 2019-04-12 15:42
 * @Description:
 */
@Service
public class MoiraiDepartmentServiceImpl implements MoiraiDepartmentService {

    private Logger logger = LoggerFactory.getLogger(MoiraiDepartmentServiceImpl.class);
    @Resource
    private MoiraiDepartmentMapper moiraiDepartmentMapper;

    @Resource
    private MoiraiDepartmentUserMapper moiraiDepartmentUserMapper;

    @Resource
    private MoiraiTenantMapper moiraiTenantMapper;

    @Resource
    private MoiraiUserMapper moiraiUserMapper;

    @Resource
    private SeqnumFeignClient seqnumFeignClient;

    @Override
    public MoiraiDepartment addDefault(MoiraiDepartment moiraiDepartment) {
        //校验租户是否存在
        Long tenantId = moiraiDepartment.getTenantId();
        MoiraiTenant tenant = moiraiTenantMapper.selectByPrimaryKey(tenantId);
        if (null == tenant) {
            //不存在，返回
            //TODO 租户锁定？
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_TENANT_IS_NULL);
        } else {
            //查询该租户是否已存在默认部门（防止默认部门有多个）
            List<MoiraiDepartment> defaultDepartment = moiraiDepartmentMapper.selectByTenantId(tenantId);
            if (!defaultDepartment.isEmpty()) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_DEPARTMENT_DEFAULTDEPART_ERROR);
            }
            //存在，添加默认部门，名称同租户名称一致
            moiraiDepartment.setName(tenant.getTenantName());
            moiraiDepartment.setCreateTime(DateTimeUtils.nowTimeLong());
            moiraiDepartment.setpId(0L);
            moiraiDepartment.setDelFlag("N");
            moiraiDepartment.setDepartmentId(seqnumFeignClient.getNum(Constants.MOIRAI_USER));
            moiraiDepartmentMapper.insertSelective(moiraiDepartment);
        }
        return moiraiDepartment;
    }

    @Override
    public MoiraiDepartment add(MoiraiDepartment moiraiDepartment) {
        //校验上级部门是否存在
        MoiraiDepartment pDepartment = moiraiDepartmentMapper.selectByPrimaryKey(moiraiDepartment.getpId());
        if (null == pDepartment) {
            //不存在，返回
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_DEPARTMENT_PID_ERROR);
        } else {
            //存在，添加下级部门
            moiraiDepartment.setTenantId(pDepartment.getTenantId());
            moiraiDepartment.setCreateTime(DateTimeUtils.nowTimeLong());
            moiraiDepartment.setDepartmentId(seqnumFeignClient.getNum(Constants.MOIRAI_USER));
            moiraiDepartment.setDelFlag("N");
            moiraiDepartmentMapper.insertSelective(moiraiDepartment);
        }
        return moiraiDepartment;
    }

    @Override
    public void update(MoiraiDepartment moiraiDepartment) {
        moiraiDepartment.setModifyTime(DateTimeUtils.nowTimeLong());
        //防止通过接口修改以下内容
        moiraiDepartment.setDelFlag(null);
        moiraiDepartment.setCreateTime(null);
        moiraiDepartment.setCreater(null);
        moiraiDepartment.setTenantId(null);
        moiraiDepartment.setpId(null);
        moiraiDepartmentMapper.updateByPrimaryKeySelective(moiraiDepartment);
    }

    @Transactional
    @Override
    public void delete(MoiraiDepartment moiraiDepartment) {
        Long departmentId = moiraiDepartment.getDepartmentId();
        //校验是否存在下级部门
        List<MoiraiDepartment> childen = moiraiDepartmentMapper.selectByPid(departmentId);
        if (childen.isEmpty()) {
            //不存在，删除本部门，同时删除部门与成员的关系
            moiraiDepartment.setDelFlag("Y");
            moiraiDepartmentMapper.updateByPrimaryKeySelective(moiraiDepartment);
        } else {
            //存在，不可以删除
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_DEPARTMENT_DELETE_ERROR);
        }
    }

    @Override
    public List<MoiraiDepartment> findDepartmentsList(MoiraiDepartment moiraiDepartment) {
        return moiraiDepartmentMapper.selectByTenantId(moiraiDepartment.getTenantId());
    }

    @Override
    public List<MoiraiDepartment> findDepartmentsTree(MoiraiDepartment moiraiDepartment) {
        List<MoiraiDepartment> moiraiDepartmentList = findDepartmentsList(moiraiDepartment);
        //转化为树形
        return getDeaprtmentTree(moiraiDepartmentList, 0L);
    }

    @Override
    public void deleteDepartmentUser(List<MoiraiDepartmentUser> list) {
        moiraiDepartmentUserMapper.deleteBatch(list);
    }

    @Override
    public List<MoiraiUser> findUserWaitAdd(MoiraiDepartment moiraiDepartment) {
        return moiraiUserMapper.findUserWaitAdd(moiraiDepartment.getTenantId(), moiraiDepartment.getDepartmentId());
    }

    @Transactional
    @Override
    public BWJsonResult addUsers2Department(MoiraiDepartmentUserAddModel model) {
        BWJsonResult result = new BWJsonResult();
        List<JSONObject> data = new ArrayList<>();
        JSONObject json;
        //TODO 校验租户id 和 部门是不是一致的
        Long departmentId = model.getDepartmentId();
        Integer postType = model.getPostType();
        String creater = model.getCreater();
        List<MoiraiDepartmentUser> userList = new ArrayList<>();
        MoiraiDepartmentUser moiraiDepartmentUser;
        for (MoiraiUser user : model.getUserList()) {
            if (null == user.getUserId()) {
                json = new JSONObject();
                json.put("result", "成员信息不完整");
                data.add(json);
                continue;
            }
            //校验用户信息
            json = new JSONObject();
            Long userId = user.getUserId();
            json.put("userId", userId);
            MoiraiUser moiraiUser = moiraiUserMapper.selectByPrimaryKey(userId);
            if (null == moiraiUser) {
                json.put("result", "成员信息不存在");
                data.add(json);
                continue;
                //throw new MoiraiUserException(MoiraiErrorEnum.MOIRAI_ACCOUNT_NOT_EXISTS_ERROR);
            }
            moiraiDepartmentUser = new MoiraiDepartmentUser();
            moiraiDepartmentUser.setUserId(userId);
            moiraiDepartmentUser.setDepartmentId(departmentId);
            moiraiDepartmentUser.setId(seqnumFeignClient.getNum(Constants.MOIRAI_USER));
            moiraiDepartmentUser.setPostType(postType);
            moiraiDepartmentUser.setCreater(creater);
            moiraiDepartmentUser.setCreateTime(DateTimeUtils.nowTimeLong());
            userList.add(moiraiDepartmentUser);
            json.put("result", "成员添加成功");
            data.add(json);
        }
        result.addData(data);
        if (!userList.isEmpty()) {
            moiraiDepartmentUserMapper.insertBatch(userList);
        }
        return result;
    }

    @Override
    public List<MoiraiDepartmentUserView> findDepartmentUser(MoiraiDepartment department) {
        return moiraiDepartmentUserMapper.findDepartmentUser(department);
    }


    private List<MoiraiDepartment> getDeaprtmentTree(List<MoiraiDepartment> departments, Long id) {
        if (departments.isEmpty()) {
            return null;
        }
        List<MoiraiDepartment> list = new ArrayList<>();
        List<MoiraiDepartment> listContinue = new ArrayList<>(departments);

        for (MoiraiDepartment md : departments) {
            if (md.getpId().equals(id)) {//从顶级菜单开始pid=0
                listContinue.remove(md);
                md.setChildren(getDeaprtmentTree(listContinue, md.getDepartmentId()));
                list.add(md);
            }
        }

        if (list.size() == 0) {
            return null;
        }
        return list;
    }
}
