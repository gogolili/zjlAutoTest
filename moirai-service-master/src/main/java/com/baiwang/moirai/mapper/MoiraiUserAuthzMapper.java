package com.baiwang.moirai.mapper;

import com.baiwang.moirai.model.role.MoiraiResource;
import com.baiwang.moirai.model.role.MoiraiUserAuthz;
import com.baiwang.moirai.model.role.MoiraiUserAuthzExample;
import com.baiwang.moirai.model.role.MoiraiUserAuthzOrg;
import com.baiwang.moirai.model.user.MoiraiUserCondition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@Mapper
public interface MoiraiUserAuthzMapper {

    List<MoiraiUserAuthz> selectByBean(MoiraiUserAuthz moiraiUserAuthz);

    int deleteByExample(MoiraiUserAuthzExample example);

    int deleteByPrimaryKey(Long uorId);

    int insert(MoiraiUserAuthz record);

    int insertSelective(MoiraiUserAuthz record);

    List<MoiraiUserAuthz> selectByExample(MoiraiUserAuthzExample example);

    MoiraiUserAuthz selectByPrimaryKey(Long uorId);

    int updateByPrimaryKeySelective(MoiraiUserAuthz record);

    int updateByPrimaryKey(MoiraiUserAuthz record);
    
    int batchInsert(@Param("records")List<MoiraiUserAuthz> records);
    
    List<MoiraiUserAuthz> selectDiffAuthOrg(MoiraiUserAuthzOrg example);

    int findCountByRoleId(Long roleId);

    List<MoiraiUserAuthzOrg> getUserAuthzOrg(MoiraiUserCondition condition);

    List<MoiraiUserAuthz> findAuthRoleListByUseId(Long userId);

    List<MoiraiResource> selectResourceByUser(@Param("userId") Long userId, @Param("orgId") Long orgId);

    List<MoiraiUserAuthz> getUserAuthByResourceId(@Param("userId") Long userId, @Param("resourceId") Long resourceId);

    List<MoiraiUserAuthz> getUserAuthByUserId(@Param("userId") Long userId, @Param("roleId") Long roleId);

    List<MoiraiUserAuthz> selectTopOrgUserAuth(@Param("userId") Long userId, @Param("tenantId") Long tenantId);
}