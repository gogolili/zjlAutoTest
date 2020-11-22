package com.baiwang.moirai.mapper;

import com.baiwang.moirai.model.log.BWCloudOperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 日志Dao
 */
@Mapper
public interface BWCloudOperationLogMapper {
    /**
     * 根据id删除一条日志记录
     */
    int deleteByPrimaryKey(String id) throws Exception;

    /**
     * 插入一条日志记录,插入全部字段
     */
    int insert(BWCloudOperationLog record) throws Exception;

    /**
     * 插入一条日志记录,可选择性的插入部分字段
     */
    int insertSelective(BWCloudOperationLog record) throws Exception;

    /**
     * 根据主键id,查询一条日志记录
     */
    BWCloudOperationLog selectByPrimaryKey(String id) throws Exception;

    /**
     * 根据主键id,更新日志记录的部分字段,id必填
     */
    int updateByPrimaryKeySelective(BWCloudOperationLog record) throws Exception;

    /**
     * 根据主键id,更新日志记录的全部字段,id必填
     */
    int updateByPrimaryKey(BWCloudOperationLog record) throws Exception;

    /**
     * 获取模块名称
     */
    List<String> selectModelName() throws Exception;

    /**
     * 获取操作结果 成功、失败
     */
    List<String> selectOperationResult() throws Exception;

    /**
     * 根据条件查询日志基本信息
     */
    List<BWCloudOperationLog> selectLogBasicInfo(BWCloudOperationLog log) throws Exception;

    /**
     * 定时删除日志方法,仅在定时任务中被使用
     */
    int deleteLogTimedTask(String clearDate) throws Exception;

    /**
     * 定时逻辑删除日志方法,仅在定时任务中被使用
     */
    int logicDeleteLogTimedTask(String clearDate) throws Exception;
}