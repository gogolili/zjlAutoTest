package com.baiwang.moirai.mapper;

import com.baiwang.moirai.model.scale.MoiraiUserDataScope;
import com.baiwang.moirai.model.scale.MoiraiUserDataScopeExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;
@Service
@Mapper
public interface MoiraiUserDataScopeMapper {

    int deleteByExample(MoiraiUserDataScopeExample example);

    int insertSelective(MoiraiUserDataScope record);

    List<MoiraiUserDataScope> selectByExample(MoiraiUserDataScopeExample example);

    int updateByPrimaryKeySelective(MoiraiUserDataScope record);
    
    int batchInsert(@Param("records") List<MoiraiUserDataScope> records);

    List<MoiraiUserDataScope> selectProductDataScope(MoiraiUserDataScope dataScope);
    /**
     * 通过用户id 查询用户数据范围
     * @param userId 用户id
     * @return
     */
    MoiraiUserDataScope selectByUserId(Long userId);
}