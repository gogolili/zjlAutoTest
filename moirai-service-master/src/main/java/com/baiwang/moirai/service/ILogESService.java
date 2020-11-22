package com.baiwang.moirai.service;

import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.model.log.BWCloudOperationLog;
import com.github.pagehelper.PageInfo;

import java.text.ParseException;

/**
 * ES服务API
 */
public interface ILogESService {


    /**
     * 根据条件查询日志基本信息
     */
    PageInfo<BWCloudOperationLog> findLogBasicInfo(BWCloudOperationLog log) throws MoiraiException, ParseException;

    public BWCloudOperationLog findLogDetailsById(BWCloudOperationLog log) throws MoiraiException;
}