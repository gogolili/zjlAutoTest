package com.baiwang.moirai.mapper;

import com.baiwang.moirai.model.sys.SysDict;
import com.baiwang.moirai.model.sys.SysDictCondition;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

import java.util.List;
@Mapper
@Service
public interface SysDictMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SysDict sysDict);

    int insertSelective(SysDict sysDict);

    SysDict selectByPrimaryKey(Long id);

    List<SysDict> selectWholeDictInfo(SysDict sysDict);

    List<SysDict> selectByBean(SysDictCondition sysDictCondition);

    int updateByPrimaryKeySelective(SysDict sysDict);

    int updateByPrimaryKey(SysDict sysDict);

    List<SysDict> getSysDictBatch(List<String> dictTypeList);
}