package com.baiwang.moirai.serviceimpl;

import com.alibaba.fastjson.JSONObject;
import com.baiwang.cloud.common.enumutil.ErrorType;
import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.cloud.common.model.ErrorMessage;
import com.baiwang.moirai.common.WebContext;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.es.ElasticSearchConfig;
import com.baiwang.moirai.es.ElasticsearchPage;
import com.baiwang.moirai.es.ElasticsearchTemplate;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.mapper.MoiraiOrgMapper;
import com.baiwang.moirai.model.log.BWCloudOperationLog;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.service.ILogESService;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Component
public class LogESServiceImpl implements ILogESService {

    private static final Logger logger = LoggerFactory.getLogger(LogESServiceImpl.class);

    @Autowired
    private ElasticsearchTemplate template;

    @Autowired
    private ElasticSearchConfig config;

    @Autowired
    private MoiraiOrgMapper moiraiOrgMapper;

    @Override
    public PageInfo<BWCloudOperationLog> findLogBasicInfo(BWCloudOperationLog log) throws MoiraiException, ParseException {
        // 设置分页信息
        log.setPageNum(log.getPageNo());
        log.setPageNum(log.getPageNum() == 0 ? 0 : log.getPageNum() >= 5000 ? 5000 : log.getPageNum());
        log.setPageSize(log.getPageSize() == 0 ? 15 : log.getPageSize() >= 50 ? 50 : log.getPageSize());
//        log.setPageSize(10);
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
            SimpleDateFormat utc = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            String utcStr = utc.format(parse);
            log.setOperationStartTime(utcStr);
        } else {
            // yyyy-MM-dd HH:mm:ss格式字符串->Date
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date parse = simpleDateFormat.parse(log.getOperationStartTime());
            // Date->utc格式字符串
            SimpleDateFormat utc = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            String utcStr = utc.format(parse);
            log.setOperationStartTime(utcStr);
        }
        if (StringUtils.isBlank(log.getOperationEndTime())) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String operationEndTime = sdf.format(new Date());
            String now = operationEndTime.concat(" 23:59:59");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date parse = simpleDateFormat.parse(now);
            SimpleDateFormat utc = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            String utcStr = utc.format(parse);
            log.setOperationEndTime(utcStr);
        } else {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date parse = simpleDateFormat.parse(log.getOperationEndTime());
            SimpleDateFormat utc = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            String utcStr = utc.format(parse);
            log.setOperationEndTime(utcStr);
        }
        PageInfo<BWCloudOperationLog> pageInfo = new PageInfo();
        try {
            ElasticsearchPage elasticsearchPage = template.searchDataPage2(config.getESIndex(), config.getType(), log.getPageNum(), log.getPageSize(), log, null, null);
            List<Map<String, Object>> recordList = elasticsearchPage.getRecordList();
            ArrayList<BWCloudOperationLog> list = new ArrayList<>();
            for (int i = 0; i < recordList.size(); i++) {
                Map<String, Object> map = recordList.get(i);
                MoiraiOrg moiraiOrg = new MoiraiOrg();
                moiraiOrg.setOrgId(log.getOrgId());
                MoiraiOrg moiraiOrg1 = moiraiOrgMapper.selectOneOrg(moiraiOrg);
                map.put("orgName", moiraiOrg1.getOrgName());
                boolean success = false;
                if (map.containsKey("success")) {
                    success = (boolean) map.get("success");
                }
                String jsonString = JSONObject.toJSONString(map);
                BWCloudOperationLog bwCloudOperationLog = JSONObject.parseObject(jsonString, BWCloudOperationLog.class);
                bwCloudOperationLog.setIp(StringUtils.substringBefore(bwCloudOperationLog.getIp(), ","));
                bwCloudOperationLog.setCreateTimeStringFormat(bwCloudOperationLog.getCreateTime());
                // 取出从es查询得到的success，若等于true设置成功false设置失败
                if (success) {
                    bwCloudOperationLog.setSuccess("成功");
                } else {
                    bwCloudOperationLog.setSuccess("失败");
                }
//            StringBuffer sb = new StringBuffer();
//            if (StringUtils.isNotBlank(bwCloudOperationLog.getOptUserAccount())) {
//                sb.append(bwCloudOperationLog.getOptUserAccount());
//            }
//            if (StringUtils.isNotBlank(bwCloudOperationLog.getAction())) {
//                sb.append(bwCloudOperationLog.getAction());
//            }
//            if (StringUtils.isNotBlank(bwCloudOperationLog.getSuccess())) {
//                sb.append(bwCloudOperationLog.getSuccess());
//            }
//            bwCloudOperationLog.setAction(sb.toString());
                list.add(bwCloudOperationLog);
            }
            pageInfo.setList(list);
            pageInfo.setTotal(elasticsearchPage.getRecordCount());
        } catch (Exception e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_LOG_MONITOR_ERROR;
            logger.error(new ErrorMessage(requestURI,errorEnum.getCode(),errorEnum.getMsg(), ErrorType.CustomerError).toString(),e);

        }
        // 查询es引擎

        return pageInfo;
    }

    @Override
    public BWCloudOperationLog findLogDetailsById(BWCloudOperationLog log) throws MoiraiException {
        String esIndex = StringUtils.substringBeforeLast(config.getESIndex(), "*")
                .concat(".").concat(log.getCreateTimeStringFormat().substring(0, 10)).concat(".log");
        Map<String, Object> map = template.searchDataById(esIndex, config.getType(), log.getId(), null);
        //取出从es查询得到的success，若等于true设置成功false设置失败
        boolean success = false;
        if (map.containsKey("success")) {
            success = (boolean) map.get("success");
        }
        String jsonString = JSONObject.toJSONString(map);
        BWCloudOperationLog bwCloudOperationLog = JSONObject.parseObject(jsonString, BWCloudOperationLog.class);
        bwCloudOperationLog.setIp(StringUtils.substringBefore(bwCloudOperationLog.getIp(), ","));
        MoiraiOrg moiraiOrg = new MoiraiOrg();
        moiraiOrg.setOrgId(log.getOrgId());
        MoiraiOrg moiraiOrg1 = moiraiOrgMapper.selectOneOrg(moiraiOrg);
        bwCloudOperationLog.setOrgName(moiraiOrg1.getOrgName());
        String stackTrace = bwCloudOperationLog.getStackTrace();
        if (success) {
            bwCloudOperationLog.setSuccess("成功");
        } else {
            bwCloudOperationLog.setSuccess("失败");
        }
        bwCloudOperationLog.setCreateTimeStringFormat(bwCloudOperationLog.getCreateTime());
        if (StringUtils.isNotBlank(stackTrace)) {
            String s = StringUtils.substringBefore(stackTrace, "\n");
            bwCloudOperationLog.setMsg(s);
        }
        // 如果返回值为空,把出错的异常信息返回去
        if (StringUtils.isBlank(bwCloudOperationLog.getReturnValue())) {
            MoiraiException systemException = new MoiraiException(bwCloudOperationLog.getCode(), bwCloudOperationLog.getMsg());
            BWJsonResult<Object> objectBWJsonResult = new BWJsonResult<>(systemException);
            bwCloudOperationLog.setReturnValue(JSONObject.toJSONString(objectBWJsonResult));
        } else {
            String returnValue = bwCloudOperationLog.getReturnValue();
            Map map1 = (Map) JSONObject.parse(returnValue);
            if (!"0".equals((String) map1.get("errorCode"))) {
                bwCloudOperationLog.setMsg((String) map1.get("errorMsg"));
                bwCloudOperationLog.setCode((String) map1.get("errorCode"));
            }
        }
        return bwCloudOperationLog;
    }
}