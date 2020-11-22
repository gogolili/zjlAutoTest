package com.baiwang.moirai.mapper;

import com.baiwang.moirai.model.role.MoiraiProductResource;
import com.baiwang.moirai.model.role.MoiraiResource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

@Service
@Mapper
public interface MoiraiProductResourceMapper {

    int deleteByPrimaryKey(Long productResourceId);

    int deleteByBean(MoiraiProductResource record);

    int insert(MoiraiProductResource record);

    int insertSelective(MoiraiProductResource record);

    int insertBatchInfo(List<MoiraiProductResource> listMoiraiProductResource);

    MoiraiProductResource selectByPrimaryKey(Long productResourceId);

    List<MoiraiProductResource> selectByBean(MoiraiProductResource moiraiProductResource);

    List<MoiraiProductResource> selectBatchInfo(List<Long> idList);

    int updateByPrimaryKeySelective(MoiraiProductResource record);

    int updateByPrimaryKey(MoiraiProductResource record);

    List<HashMap<Long, Long>> selectprimaryKey(@Param(value = "productId") Long productId);

    void deleteProductResource(@Param(value = "productId") Long productId, @Param(value = "idList") List<Long> idList);
}