package com.baiwang.moirai.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.baiwang.moirai.model.oauth.MoiraiOauth2Client;
@Mapper
@Service
public interface MoiraiOauth2ClientMapper {

    int deleteByPrimaryKey(Long id);

    int insert(MoiraiOauth2Client record);

    int insertSelective(MoiraiOauth2Client record);

    MoiraiOauth2Client selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(MoiraiOauth2Client record);

    int updateByPrimaryKey(MoiraiOauth2Client record);

    MoiraiOauth2Client selectByClientId(String id);

    List<MoiraiOauth2Client> selectByClient(MoiraiOauth2Client record);

    List<MoiraiOauth2Client> selectAllOrCondition(MoiraiOauth2Client record);
}