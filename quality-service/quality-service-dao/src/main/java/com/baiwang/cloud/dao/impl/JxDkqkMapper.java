package com.baiwang.cloud.dao.impl;


import com.baiwang.cloud.common.model.JxdkqkPageParam;
import com.baiwang.cloud.common.model.PageResult;
import com.baiwang.cloud.common.model.JxDkqk;
import feign.Param;
import org.springframework.stereotype.Service;


import java.util.List;

/**
 * (JxDkqk)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-04 15:00:00
 */

@Service
public interface JxDkqkMapper<T> {

    /**
     * 分页查询
     * @param jxdkqkPageParam 实例对象
     * @return 对象列表
     */
    List<JxDkqk> queryJxDkqkByConditions(JxdkqkPageParam jxdkqkPageParam);

    /**
     * 按条件查询
     * @param jxDkqk
     * @return
     */
    List<JxDkqk> queryJxDkqkByConditions(JxDkqk jxDkqk);


    /**
     * 新增数据
     *
     * @param jxDkqk 实例对象
     * @return 影响行数
     */
    int insert(JxDkqk jxDkqk);

    /**
     * 修改数据
     *
     * @param jxDkqk 实例对象
     * @return 影响行数
     */
    int update(JxDkqk jxDkqk);

    /**
     * 通过主键删除数据
     *
     * @param jxDkqk 主键
     * @return 影响行数
     */
    int deleteJxDkqk(JxDkqk jxDkqk);

}