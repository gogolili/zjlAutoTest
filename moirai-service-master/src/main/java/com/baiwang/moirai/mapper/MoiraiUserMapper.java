package com.baiwang.moirai.mapper;

import com.baiwang.moirai.model.role.MoiraiUserAuthz;
import com.baiwang.moirai.model.user.MoiraiEmailCheck;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.model.user.MoiraiUserCondition;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

@Service
@Mapper
public interface MoiraiUserMapper {

    int deleteByPrimaryKey(Long userId);

    int insertSelective(MoiraiUser record);

    MoiraiUser selectByPrimaryKey(Long userId);

    List<MoiraiUser> selectByIds(List kpyUserIds);

    int updateByPrimaryKeySelective(MoiraiUser record);

    List<MoiraiUser> findUserByCondition(MoiraiUserCondition moiraiUserCondition);

    List<MoiraiUser> findUserListByCondition(MoiraiUserCondition moiraiUserCondition);

    MoiraiUser getUserByUserAccount(String userAccount);

    MoiraiUser getUserByUserEmail(@Param("userEmail") String userEmail, @Param("tenantId") Long tenanId);

    List<Map<String, Integer>> batchEmailCheckRepetition(MoiraiEmailCheck moiraiEmailCheck);

    List<MoiraiUser> getUserByRoleId(MoiraiUserCondition moiraiUserCondition);

    int delUserRoleByUserIdRoleId(MoiraiUserCondition moiraiUserCondition);

    List<MoiraiUser> findUserByTenantId(MoiraiUserCondition moiraiUserCondition);

    int addBatch(List<MoiraiUser> record);

    /** 手机号租户下唯一 **/
    MoiraiUser getUserByTelephone(@Param("telephone") String telephone, @Param("tenantId") Long tenanId);

    int insertUserHistory(MoiraiUser moiraiUser);

    int deleteSimplePhone(String telephone);

    /**
     * 查询某部门可以添加的成员列表
     *
     * @param tenantId 租户id
     * @param departmentId 部门id
     * @return
     */
    List<MoiraiUser> findUserWaitAdd(@Param("tenantId") Long tenantId, @Param("departmentId") Long departmentId);

    /**
     * 通过租户id 查询用户列表 CP使用，不包含 机构管理员
     *
     * @param tenantId
     * @return
     */
    List<MoiraiUser> selectByTenantId(@Param("tenantId") Long tenantId);

    MoiraiUser getCUserByTelephone(String telephone);

    int findUserCountByCondition(MoiraiUserCondition moiraiUserCondition);

    int updateByCondition(MoiraiUserCondition moiraiUserCondition);

    List<MoiraiUser> selectUserByAccount(List<MoiraiUser> list);

    List<MoiraiUser> selectUserByEmail(@Param("list") List<String> list, @Param("tenantId") Long tenantId);

    List<MoiraiUser> selectUserByPhone(@Param("list") List<String> list, @Param("tenantId") Long tenantId);

    MoiraiUser selectOneUser(MoiraiUserCondition condition);

    /**
     * 查询租户管理员列表
     */
    List<MoiraiUser> selectTenantAccountUserList(@Param("list") List<Long> list);

    @MapKey("orgId")
    Map<Long, MoiraiUser> selectAdminUser(List<Long> list);

    List<MoiraiUserCondition> findBUserListByCondition(MoiraiUserCondition condition);

    List<MoiraiUser> selectUserListByAuth(MoiraiUserAuthz query);
}