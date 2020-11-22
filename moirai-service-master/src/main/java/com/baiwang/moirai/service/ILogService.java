package com.baiwang.moirai.service;

import com.baiwang.cloud.common.exception.SystemException;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.model.log.BWCloudOperationLog;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.List;

/**
 * 日志API
 */
@Service("logService")
public interface ILogService {


    /**
     * 根据id获取日志详情
     */
    BWCloudOperationLog findLogDetailsById(String id) throws SystemException;

    public List<String> findOperationResult() throws SystemException;

    public PageInfo<BWCloudOperationLog> findLogBasicInfo(BWCloudOperationLog log) throws MoiraiException, ParseException;

}