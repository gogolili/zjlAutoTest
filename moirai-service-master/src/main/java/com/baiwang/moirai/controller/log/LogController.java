package com.baiwang.moirai.controller.log;

import com.baiwang.cloud.common.enumutil.SystemErrorEnum;
import com.baiwang.cloud.common.exception.SystemException;
import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.cloud.logaop.model.Log;
import com.baiwang.moirai.api.MoiraiLogSvc;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.es.ElasticsearchTemplate;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.model.log.BWCloudOperationLog;
import com.baiwang.moirai.service.ILogESService;
import com.baiwang.moirai.service.ILogService;
import com.baiwang.moirai.service.PrintLogWriterService;
import com.github.pagehelper.PageInfo;
import java.text.ParseException;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
public class LogController implements MoiraiLogSvc {

    private static final Logger logger = LoggerFactory.getLogger(LogController.class);

    /**
     * 云端日志服务
     */
    @Autowired
    private ILogESService logESService;

    /***/
    @Value("${baiwang.cloud.operation.log.depositary:ES}")
    private String depositary;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private ILogService logService;

    @Autowired
    PrintLogWriterService writerService;

    @ResponseBody
    public BWJsonResult<List<BWCloudOperationLog>> findLogBasicInfo(
        @RequestBody BWCloudOperationLog log) throws MoiraiException, ParseException {
        long start = System.currentTimeMillis();
        logger.info("【Controller】- LogController.findLogBasicInfo根据条件查询日志基本信息接口请求入参:{}", log);
        if (StringUtils.isBlank(log.getOrgIds())) {
//            throw new SystemException("4598", "组织机构编号必传[orgIds]");
        }
        // 若根据ip查询,校验ip。多个ip以英文逗号分割
        if (StringUtils.isNotBlank(log.getIp())) {
            String[] split = log.getIp().split(",");
            for (int i = 0; i < split.length; i++) {
                checkIp(split[i]);
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < split.length; i++) {
                if (i == split.length - 1) {
                    sb.append(split[i]);
                } else {
                    sb.append(split[i]).append(", ");
                }

            }
            log.setIp(sb.toString());
        }
        PageInfo<BWCloudOperationLog> logBasicInfo = null;
        if ("DB".equals(depositary)) {
            logBasicInfo = logService.findLogBasicInfo(log);
        } else {
            logBasicInfo = logESService.findLogBasicInfo(log);
        }
        BWJsonResult<List<BWCloudOperationLog>> result = new BWJsonResult(logBasicInfo.getList());
        result.setTotal(Integer.valueOf(String.valueOf(logBasicInfo.getTotal())));
        result.setMessage("查询日志基本信息成功");
        logger.info("【Controller】- LogController.findLogBasicInfo根据条件查询日志基本信息服务成功,服务耗时:{} ms,返回前端数据:{}",
            System.currentTimeMillis() - start, result);
        return result;
    }

    /**
     * 获取日志详情
     */
    @PostMapping("/findLogDetailsById")
    @ResponseBody
    public BWJsonResult<BWCloudOperationLog> findLogDetailsById(@RequestBody BWCloudOperationLog log) throws SystemException {
        long start = System.currentTimeMillis();
        logger.info("【Controller】- LogController.findLogDetailsById获取日志详情接口入参{}", log);
        String id = log.getId();
        if ("ES".equals(depositary)) {

            if (StringUtils.isBlank(id) || StringUtils.isBlank(log.getCreateTimeStringFormat())) {
                throw new SystemException(SystemErrorEnum.OUTPUT_PARAM_EMPTY_ERROR.getCode(), "日志id和操作时间不能为空");
            }
        } else {
            if (StringUtils.isBlank(id)) {
                throw new SystemException(SystemErrorEnum.OUTPUT_PARAM_EMPTY_ERROR.getCode(), "日志id不能为空");
            }
        }
        logger.info("findLogBasicInfo 日志详情 depositary = 【{}】",depositary);
        BWCloudOperationLog logDetails;
        if ("DB".equals(depositary)) {
            logDetails = logService.findLogDetailsById(id);
        } else {
            logDetails = logESService.findLogDetailsById(log);
        }
        BWJsonResult<BWCloudOperationLog> result = new BWJsonResult<>(logDetails);
        result.setMessage("获取日志详情成功");
        result.setTotal(1);
        logger.info("【Controller】-LogController.findLogDetailsById获取日志详情服务调用成功,服务耗时:{} ms,返回前端数据:{}",
                System.currentTimeMillis() - start, result);
        return result;
    }

    /**
     * 校验IP是否符合规范
     */
    public int checkIp(String ip) throws MoiraiException {
        int n = 0;
        while (ip.indexOf('.') != -1) {
            int i = ip.indexOf('.');
            n++;
            ip = ip.substring(i + 1);
        }
        if (n != 3) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_IP_NOT_STANDARD);
        }
        return n;
    }

    @Override
    public BWJsonResult createIndex(String index) {
        boolean index1 = elasticsearchTemplate.createIndex(index);
        BWJsonResult bwJsonResult = new BWJsonResult(index1);
        return bwJsonResult;
    }

    @Override
    public BWJsonResult isExistIndex(String index) {
        boolean index1 = elasticsearchTemplate.isIndexExist(index);
        BWJsonResult bwJsonResult = new BWJsonResult(index1);
        return bwJsonResult;
    }

    @RequestMapping("/log/ssoWritrLog")
    public BWJsonResult ssoWritrLog(@RequestBody Log log){
        writerService.write(log);
        BWJsonResult bwJsonResult = new BWJsonResult();
        bwJsonResult.setMessage("单点服务日志本地版写入成功");
        return bwJsonResult;
    }

}
