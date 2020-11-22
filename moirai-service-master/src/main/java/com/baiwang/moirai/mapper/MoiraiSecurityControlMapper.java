package com.baiwang.moirai.mapper;

import com.baiwang.moirai.model.role.MoiraiResourceSecurity;
import com.baiwang.moirai.model.role.MoiraiResourceSecurityCondition;
import com.baiwang.moirai.model.role.MoiraiSecurityControl;
import com.baiwang.moirai.model.role.MoiraiSecurityControlCondition;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface MoiraiSecurityControlMapper {

    int deleteByPrimaryKey(Long id);

    int insertSelective(MoiraiSecurityControl record);

    MoiraiSecurityControl selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(MoiraiSecurityControl record);

    List<MoiraiSecurityControl> selectListByCondition(MoiraiSecurityControlCondition record);

    MoiraiSecurityControl selectByName(String name);

    List<MoiraiSecurityControl> getResourceSecurityList(MoiraiResourceSecurityCondition condition);

    int deleteResourceSecurity(MoiraiResourceSecurity security);

    int insertResourceSecurity(MoiraiResourceSecurity security);

    List<MoiraiResourceSecurity> selectResourceSecurity(MoiraiResourceSecurityCondition query);

    List<MoiraiSecurityControl> queryUnbindSecurity(MoiraiResourceSecurityCondition condition);
}