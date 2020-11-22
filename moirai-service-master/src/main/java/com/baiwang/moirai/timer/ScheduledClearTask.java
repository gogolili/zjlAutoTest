package com.baiwang.moirai.timer;

import com.baiwang.cloud.common.enumutil.ErrorType;
import com.baiwang.cloud.common.exception.SystemException;
import com.baiwang.cloud.common.model.ErrorMessage;
import com.baiwang.moirai.common.WebContext;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.mapper.BWCloudOperationLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@ConditionalOnProperty(name = "baiwang.cloud.operation.log.clear.job",havingValue = "bwCloudOperationLog",matchIfMissing = false)
@Component
public class ScheduledClearTask {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledClearTask.class);
    /**
     * 记录清除日志定时任务被执行的次数
     */
    private static int taskCount = 0;
    /**
     * redis 工具服务
     */
    @Autowired
    @Qualifier("redisTrafficTemplate")
    private RedisTemplate redisTemplate;
    /**
     * 日志Dao
     */
    @Autowired
    private BWCloudOperationLogMapper logMapper;
    /**
     * 清除日志定时任务名称
     */
    @Value("${baiwang.cloud.operation.log.clear.job:bwCloudOperationLog}")
    private String logClearJob;

    @Value("${use.method}")
    private boolean useFlag;

    @Scheduled(cron = "0 0 0 * * ?")
//    @Scheduled(cron = "0/10 * * * * ?")
    public void deleteLogScheduledTask() throws SystemException {
        if (!useFlag) {
            if (!allowExecute(logClearJob)) {
                return;
            }
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String clearDate = sdf.format(new Date());
                int number = logMapper.deleteLogTimedTask(clearDate);
                logger.info("【Task】- 第" + ++taskCount + "次执行定时任务,此时共计删除日志 " + number + " 条");
            } catch (Exception e) {
                String requestURI = WebContext.getRequest().getRequestURI();
                MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_LOG_MONITOR_ERROR;
                logger.error(new ErrorMessage(requestURI,errorEnum.getCode(),errorEnum.getMsg(), ErrorType.CustomerError).toString(),e);
                throw new SystemException("6729", "日志定时任务执行失败,请及时解决");
            }
        }
    }

    private boolean allowExecute(String logClearJob) throws SystemException {
        int max = 10000;
        int min = (int) Math.round(Math.random() * 8000);
        long sleepTime = Math.round(Math.random() * (max - min));
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            throw new SystemException("1230", e.getMessage());
        }
        logger.info("【Task】-日志删除定时任务{}线程睡了{} ms", logClearJob, sleepTime);
        if (!redisTemplate.hasKey(logClearJob)) {
            ValueOperations<Serializable, Object> operations = this.redisTemplate.opsForValue();
            operations.set(logClearJob, UUID.randomUUID().toString());
            this.redisTemplate.expire(logClearJob, 10L, TimeUnit.SECONDS);
            return true;
        }
        logger.info("【Task】-日志删除定时任务{}已被其他服务执行", logClearJob);
        return false;
    }

}