/*
 * @项目名称: Moirai
 * @文件名称: MoiraiSecretKeyMapper.java
 * @Date: 17-12-5 下午7:27
 * @author Lance cui
 *
 */

package com.baiwang.moirai.mapper;

import com.baiwang.moirai.model.user.MoiraiSecretKey;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Mapper
public interface MoiraiSecretKeyMapper {

    List<MoiraiSecretKey> selectAllSecretKey();

    int deleteByPrimaryKey(Long skId);

    int insert(MoiraiSecretKey record);

    int insertSelective(MoiraiSecretKey record);

    MoiraiSecretKey selectByPrimaryKey(Long skId);

    int updateByPrimaryKeySelective(MoiraiSecretKey record);

    int updateByPrimaryKey(MoiraiSecretKey record);
}