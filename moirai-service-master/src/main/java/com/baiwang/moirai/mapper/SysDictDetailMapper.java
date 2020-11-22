package com.baiwang.moirai.mapper;

import com.baiwang.moirai.model.sys.SysDict;
import com.baiwang.moirai.model.sys.SysDictCondition;
import com.baiwang.moirai.model.sys.SysDictDetail;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Mapper
@Service
public interface SysDictDetailMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SysDictDetail record);

    int insertSelective(SysDict record);

    SysDictDetail selectByPrimaryKey(Long id);

    List<SysDictDetail> selectByBean(SysDictCondition sysDictCondition);

    int updateByPrimaryKeySelective(SysDictDetail record);

    int updateByPrimaryKey(SysDictDetail record);

    void batchDel(SysDictCondition sysDictCondition);

    void addDetailBatch(List<SysDictDetail> list);

    /**
     * 根据字典类型 查询对应字典明细list
     * @param dictType
     * @return
     */
    List<SysDictDetail> selectDetailByDictType(String dictType);
}