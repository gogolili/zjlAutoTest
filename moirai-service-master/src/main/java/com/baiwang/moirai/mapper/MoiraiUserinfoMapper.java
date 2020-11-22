package com.baiwang.moirai.mapper;


import com.baiwang.moirai.model.user.MoiraiUserinfo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Mapper
public interface MoiraiUserinfoMapper {

    int deleteByUserId(Long userinfoId);

    int insert(MoiraiUserinfo record);

    int updateByPrimaryKeySelective(MoiraiUserinfo record);

    int  updateByUserId(MoiraiUserinfo record);

    MoiraiUserinfo getUserInfoByUserId(Long userId);

    MoiraiUserinfo getUserInfoById(Long userinfoId);

    int insertUserInfoHistory(MoiraiUserinfo moiraiUserinfo);

    int addBatch(List<MoiraiUserinfo> list);
    
}