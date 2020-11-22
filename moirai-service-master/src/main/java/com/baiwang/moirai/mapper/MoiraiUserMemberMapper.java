package com.baiwang.moirai.mapper;

import com.baiwang.moirai.model.user.MoiraiUserMember;
import com.baiwang.moirai.model.user.MoiraiUserMemberCondition;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Mapper
public interface MoiraiUserMemberMapper {

    List<MoiraiUserMember> selectByBean(MoiraiUserMemberCondition condition);

    /**
     * 插入数据
     *
     * @param moiraiUserMember
     */
    int insertSelective(MoiraiUserMember moiraiUserMember);

    List<MoiraiUserMemberCondition> selectByUnion(MoiraiUserMemberCondition condition);

    int updateByPrimaryKeySelective(MoiraiUserMember moiraiUserMember);

    List<MoiraiUserMember> selectLoginOrFactor(MoiraiUserMember condition);

    int updateMoreMember(List<MoiraiUserMember> list);

    MoiraiUserMember selectBindFactorTenant(MoiraiUserMember member);
}
