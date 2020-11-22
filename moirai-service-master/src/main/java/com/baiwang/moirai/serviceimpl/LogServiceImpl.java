package com.baiwang.moirai.serviceimpl;

import com.alibaba.fastjson.JSONObject;
import com.baiwang.cloud.common.enumutil.ErrorType;
import com.baiwang.cloud.common.exception.SystemException;
import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.cloud.common.model.ErrorMessage;
import com.baiwang.moirai.common.WebContext;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.mapper.BWCloudOperationLogMapper;
import com.baiwang.moirai.mapper.MoiraiOrgMapper;
import com.baiwang.moirai.model.log.BWCloudOperationLog;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.service.ILogService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 审计日志管理服务
 */
@Component
public class LogServiceImpl implements ILogService {
    private static final Logger logger = LoggerFactory.getLogger(LogServiceImpl.class);
    /**
     * 日志Dao
     */
    @Autowired
    private BWCloudOperationLogMapper bwCloudOperationLogMapper;

    @Autowired
    private MoiraiOrgMapper moiraiOrgMapper;

    

    /**
     * 根据id获取日志详情
     */
    @Override
    public BWCloudOperationLog findLogDetailsById(String id) throws SystemException {
        try {
            BWCloudOperationLog bwCloudOperationLog = bwCloudOperationLogMapper.selectByPrimaryKey(id);
            if (bwCloudOperationLog != null) {
                bwCloudOperationLog.setIp(StringUtils.substringBefore(bwCloudOperationLog.getIp(), ","));
                bwCloudOperationLog.setCreateTimeStringFormat(bwCloudOperationLog.getCreateTime());
//                StringBuffer sb = new StringBuffer();
//                if (StringUtils.isNotBlank(bwCloudOperationLog.getOptUserAccount())) {
//                    sb.append(bwCloudOperationLog.getOptUserAccount());
//                }
//                if (StringUtils.isNotBlank(bwCloudOperationLog.getAction())) {
//                    sb.append(bwCloudOperationLog.getAction());
//                }
//                if (StringUtils.isNotBlank(bwCloudOperationLog.getSuccess())) {
//                    sb.append(bwCloudOperationLog.getSuccess());
//                }
//                bwCloudOperationLog.setAction(sb.toString());
                Long orgId = bwCloudOperationLog.getOrgId();
                MoiraiOrg moiraiOrg = new MoiraiOrg();
                moiraiOrg.setOrgId(orgId);
                MoiraiOrg org = moiraiOrgMapper.selectOneOrg(moiraiOrg);
                bwCloudOperationLog.setOrgName(org.getOrgName());
                String stackTrace = bwCloudOperationLog.getStackTrace();
                if (StringUtils.isNotBlank(stackTrace)) {
                    String s = StringUtils.substringBefore(stackTrace, "\n");
                    bwCloudOperationLog.setMsg(s);
                }
                if (StringUtils.isBlank(bwCloudOperationLog.getReturnValue())) {
                    SystemException systemException = new SystemException(bwCloudOperationLog.getCode(), bwCloudOperationLog.getMsg());
                    BWJsonResult<Object> objectBWJsonResult = new BWJsonResult<>(systemException);
                    bwCloudOperationLog.setReturnValue(JSONObject.toJSONString(objectBWJsonResult));
                } else {
                    try {
                        String returnValue = bwCloudOperationLog.getReturnValue();
                        Map map1 = (Map) JSONObject.parse(returnValue);
                        if (!"0".equals((String) map1.get("errorCode"))) {
                            bwCloudOperationLog.setMsg((String) map1.get("errorMsg"));
                            bwCloudOperationLog.setCode((String) map1.get("errorCode"));
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e.getStackTrace());
                    }
                }
            }
            return bwCloudOperationLog;
        } catch (SystemException se) {
            throw se;
        } catch (Exception e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_LOG_MONITOR_ERROR;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString());
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_LOG_MONITOR_ERROR);
        }
    }

    /**
     * 获取日志操作结果
     */
    @Override
    public List<String> findOperationResult() throws SystemException {
        try {
            logger.info("【Service】- LogServiceImpl.findOperationResult 开始查库");
            List<String> operationResult = bwCloudOperationLogMapper.selectOperationResult();
            logger.info("【Service】- LogServiceImpl.findOperationResult 查库结果:{}", JSONObject.toJSONString(operationResult));
            if (operationResult == null) {
                operationResult = new ArrayList<>();
                operationResult.add("全部");
            } else {
                operationResult.add("全部");
            }
            return operationResult;
        } catch (Exception e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_LOG_MONITOR_ERROR;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString());
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_LOG_MONITOR_ERROR);
        }
    }

    /**
     * 根据条件查询日志基本信息
     */
    @Override
    public PageInfo<BWCloudOperationLog> findLogBasicInfo(BWCloudOperationLog log) throws MoiraiException, ParseException {
        if (log.getSuccess() == null || "全部".equals(log.getSuccess())) {
            log.setSuccess(null);
        }
        log.setPageNum(log.getPageNo());
        String operationStartTime1 = log.getOperationStartTime();
        String operationEndTime1 = log.getOperationEndTime();
        if (!StringUtils.isBlank(operationEndTime1) && operationEndTime1.equals(operationStartTime1)) {
            String operationStartTime = log.getOperationStartTime();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = simpleDateFormat.parse(operationStartTime);
            operationStartTime = simpleDateFormat.format(date);
            String now = operationStartTime.concat(" 23:59:59");
            logger.info("重置结束日志 ILogESService operationStartTime={}", now);
            log.setOperationEndTime(now);
        }
        // range 区间查询
        if (StringUtils.isBlank(log.getOperationStartTime())) {
            // 拼接当前时间yyyy-MM-dd HH:mm:ss格式字符串
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String operationStartTime = sdf.format(new Date());
            String now = operationStartTime.concat(" 00:00:00");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // yyyy-MM-dd HH:mm:ss格式字符串->Date
            Date parse = simpleDateFormat.parse(now);
            // Date->utc格式字符串
            SimpleDateFormat utc = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String utcStr = utc.format(parse);
            log.setOperationStartTime(utcStr);
        } else {
            // yyyy-MM-dd HH:mm:ss格式字符串->Date
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date parse = simpleDateFormat.parse(log.getOperationStartTime());
            // Date->utc格式字符串
            SimpleDateFormat utc = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String utcStr = utc.format(parse);
            log.setOperationStartTime(utcStr);
        }
        if (StringUtils.isBlank(log.getOperationEndTime())) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String operationEndTime = sdf.format(new Date());
            String now = operationEndTime.concat(" 23:59:59");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date parse = simpleDateFormat.parse(now);
            SimpleDateFormat utc = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String utcStr = utc.format(parse);
            log.setOperationEndTime(utcStr);
        } else {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date parse = simpleDateFormat.parse(log.getOperationEndTime());
            SimpleDateFormat utc = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String utcStr = utc.format(parse);
            log.setOperationEndTime(utcStr);
        }
        log.setOrgIdList(Arrays.asList(log.getOrgIds().split(",")));
        // 默认页码第1页
        log.setPageNum(log.getPageNum() == 0 ? 1 : log.getPageNum() >= 5000 ? 5000 : log.getPageNum());
        // 默认分页15条/页
        log.setPageSize(log.getPageSize() == 0 ? 15 : log.getPageSize() >= 50 ? 50 : log.getPageSize());
        try {
            logger.info("【Service】- LogServiceImpl.findLogBasicInfo 执行查库语句入参信息:{}", log.toString());
            PageHelper.startPage(log.getPageNum(), log.getPageSize());
            List<BWCloudOperationLog> logs = bwCloudOperationLogMapper.selectLogBasicInfo(log);
            logger.info("【Service】- LogServiceImpl.findLogBasicInfo 查库结果:{}", logs);
            if (logs != null && logs.size() > 0) {
                for (int i = 0; i < logs.size(); i++) {
                    logs.get(i).setIp(StringUtils.substringBefore(logs.get(i).getIp(), ","));
                    logs.get(i).setCreateTimeStringFormat(logs.get(i).getCreateTime());
//                    StringBuffer sb = new StringBuffer();
//                    if (StringUtils.isNotBlank(logs.get(i).getOptUserAccount())) {
//                        sb.append(logs.get(i).getOptUserAccount());
//                    }
//                    if (StringUtils.isNotBlank(logs.get(i).getAction())) {
//                        sb.append(logs.get(i).getAction());
//                    }
//                    if (StringUtils.isNotBlank(logs.get(i).getSuccess())) {
//                        sb.append(logs.get(i).getSuccess());
//                    }
//                    logs.get(i).setAction(sb.toString());
                    Long orgId = logs.get(i).getOrgId();
                    MoiraiOrg moiraiOrg = new MoiraiOrg();
                    moiraiOrg.setOrgId(orgId);
                    MoiraiOrg org = moiraiOrgMapper.selectOneOrg(moiraiOrg);
                    if(org != null){
                        logs.get(i).setOrgName(org.getOrgName());
                    }
                }
            } else if (logs == null) {
                logs = new ArrayList<>();
            }
            PageInfo<BWCloudOperationLog> logPage = new PageInfo<>(logs);
            return logPage;
        } catch (Exception e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_LOG_MONITOR_ERROR;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString());
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_LOG_MONITOR_ERROR);
        }
    }
}