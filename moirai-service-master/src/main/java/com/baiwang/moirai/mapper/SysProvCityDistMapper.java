package com.baiwang.moirai.mapper;

import com.baiwang.moirai.model.sys.SysProvCityDist;
import com.baiwang.moirai.model.sys.SysProvCityDistTree;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

import java.util.List;
@Mapper
@Service
public interface SysProvCityDistMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(SysProvCityDist record);

    int insertSelective(SysProvCityDist record);

    SysProvCityDist selectByPrimaryKey(Integer id);

    List<SysProvCityDist> selectByBean(SysProvCityDist sysProvCityDist);

    List<SysProvCityDist> selectByBeanVague(SysProvCityDist sysProvCityDist);

    int updateByPrimaryKeySelective(SysProvCityDist record);

    int updateByPrimaryKey(SysProvCityDist record);

    /**
     * 查询所有
     * @return
     */
    List<SysProvCityDistTree> selectAll();

    List<SysProvCityDist> selectMore();
}