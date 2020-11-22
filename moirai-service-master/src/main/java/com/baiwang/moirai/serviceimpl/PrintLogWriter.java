package com.baiwang.moirai.serviceimpl;

import com.alibaba.fastjson.JSONObject;
import com.baiwang.cloud.common.enumutil.ErrorType;
import com.baiwang.cloud.common.model.ErrorMessage;
import com.baiwang.cloud.logaop.model.Log;
import com.baiwang.cloud.logaop.spi.SystemLogWriter;
import com.baiwang.moirai.common.WebContext;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.mapper.BWCloudOperationLogMapper;
import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.model.log.BWCloudOperationLog;
import com.baiwang.moirai.service.PrintLogWriterService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 日志Service
 */
@Component
public class PrintLogWriter implements SystemLogWriter, PrintLogWriterService {
    private static final Logger logger = LoggerFactory.getLogger(PrintLogWriter.class);
    /**
     * 操作日志入库保存天数
     */
    @Value("${log.save.days:7}")
    private int clear;
    /**
     * 操作日志入库Dao
     */
    @Autowired
    private BWCloudOperationLogMapper bWCloudOperationLogMapper;

    /**
     * Description(方法功能描述) TODO 日志入库
     */
    @Override
    public void write(Log log) throws MoiraiException {
        logger.info("PrintLogWriter.write log-aop传入的操作日志对象信息为:{}", JSONObject.toJSONString(log));
        if (log == null) {
            return;
        }
        // bean拷贝
        try {
            BWCloudOperationLog operationLog = new BWCloudOperationLog();
            BeanUtils.copyProperties(log, operationLog);
            // 解析外部信息externalInfo
            Map<String, Object> externalInfo = log.getExternalInfo();
            if (externalInfo != null && !externalInfo.isEmpty()) {
                // 操作人用户账号
                if (externalInfo.containsKey("optUserAccount") && externalInfo.get("optUserAccount") != null) {
                    operationLog.setOptUserAccount((String) externalInfo.get("optUserAccount"));
                }
                // 操作人用户名
                if (externalInfo.containsKey("optUserName") && externalInfo.get("optUserName") != null) {
                    operationLog.setOptUserName((String) externalInfo.get("optUserName"));
                }
                // 操作人租户编号
                if (externalInfo.containsKey("tenantId") && externalInfo.get("tenantId") != null) {
                    operationLog.setTenantId((Long) externalInfo.get("tenantId"));
                }
                // 操作人组织机构编号
                if (externalInfo.containsKey("orgId") && externalInfo.get("orgId") != null) {
                    operationLog.setOrgId((Long) externalInfo.get("orgId"));
                }
                // 操作人税号
                if (externalInfo.containsKey("taxCode") && externalInfo.get("taxCode") != null) {
                    operationLog.setTaxCode((String) externalInfo.get("taxCode"));
                }
                // 浏览器名称
                if (externalInfo.containsKey("userAgent") && externalInfo.get("userAgent") != null) {
                    String userAgent = (String) externalInfo.get("userAgent");
                    String[] strArr = userAgent.split("/");
                    List<String> list = Arrays.asList(strArr);
                    if (!list.isEmpty() && list.size() >= 3) {
                        String s = list.get(2);
                        operationLog.setBrowserName(s.substring(StringUtils.lastIndexOf(s, " ") + 1));
                    } else if (!list.isEmpty()) {
                        String s = list.get(0);
                        operationLog.setBrowserName(s);
                    } else {
                        operationLog.setBrowserName(null);
                    }

                }
                operationLog.setExternalInfo(JSONObject.toJSONString(externalInfo));
            }
            // 判断日志信息异常堆栈长度,截取前1500字符
            if (log.getStackTrace() != null && log.getStackTrace().length() >= 2000) {
                operationLog.setStackTrace(log.getStackTrace().substring(0, 2000));
            }
            // 判断日志返回值长度,截取前8000字符,并解析操作结果。如果返回值为void,则去通过堆栈信息去解析操作结果
            if (log.getReturnValue() != null && log.getReturnValue().length() >= 8000) {
                String returnValue = log.getReturnValue();
                operationLog.setReturnValue(returnValue.substring(0, 8000));
            }
            /**成功失败解析 1.先校验日志是否收集到Execption信息，若收集到Execption则success=false */
            if (StringUtils.isNotBlank(log.getStackTrace())) {
                operationLog.setSuccess("失败");
            } else if (StringUtils.isNotBlank(log.getReturnValue())) {
                //2. 若日志returnValue字段收集到BWJsonResult对象信息，解析BWJsonResult的errorCode字段，errorCode!=0则success=false，否则success=true
                String returnValue = log.getReturnValue();
                BWJsonResult result = JSONObject.parseObject(returnValue, BWJsonResult.class);
                if (!"0".equals(result.getErrorCode())) {
                    operationLog.setSuccess("失败");
                } else {
                    operationLog.setSuccess("成功");
                }
            } else {
                /**3.若日志未收集到Execption信息，且returnValue字段未收集到BWJsonResult对象信息，说明接口返回值为非标准格式
                 如：public void getInvoiceVO(MoiraiOrg moiraiOrg,OnlineApplyInvoiceVO onlineApplyInvoiceVO)
                 这种情况若未收集到Execption信息，success=true否则success=false*/
                operationLog.setSuccess("成功");
            }
            /**成功失败解析*/
            // 根据操作时间,设置删除时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (log.getCreateTime() != null) {
                operationLog.setClearDate(sdf.format(DateUtils.addDays(log.getCreateTime(), clear)));
            } else {
                operationLog.setClearDate(sdf.format(DateUtils.addDays(log.getCreateTime(), 7)));
            }
            // 设置日志记录创建人
            operationLog.setCreator("output日志收集器");
            // 设置日志记录创建时间
            operationLog.setCtime(new Date());
            // 入库
            logger.info("PrintLogWriter.write 自定义日志输出入库日志信息:{}", JSONObject.toJSONString(operationLog));
            bWCloudOperationLogMapper.insertSelective(operationLog);
        } catch (Exception e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.Moirai_DB_ERROR;
            logger.error(new ErrorMessage(requestURI,errorEnum.getCode(),errorEnum.getMsg(), ErrorType.CustomerError).toString(),e);

            throw new MoiraiException("3306", "日志入库失败");
        }
    }
}