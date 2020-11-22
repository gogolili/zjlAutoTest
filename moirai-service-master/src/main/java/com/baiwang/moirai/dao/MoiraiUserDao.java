package com.baiwang.moirai.dao;

import com.baiwang.cloud.common.aop.aspect.ExpDataFiled;
import com.baiwang.cloud.common.context.ExtDataContext;
import com.baiwang.cloud.common.context.ExtDataContextHolder;
import com.baiwang.cloud.common.context.OperType;
import com.baiwang.moirai.mapper.MoiraiUserMapper;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.model.user.MoiraiUserCondition;
import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MoiraiUserDao {

    private static final Logger logger = LoggerFactory.getLogger(MoiraiUserDao.class);

    //注意：加注解的sql禁止使用union关键字  扩展字段不支持

    @Autowired
    MoiraiUserMapper moiraiUserMapper;


    @ExpDataFiled(TableName = "moirai_user",operType = OperType.INSERT)
    public int insertSelective(MoiraiUser moiraiUser){
        int key = moiraiUserMapper.insertSelective(moiraiUser);
        ExtDataContext extDataContext = ExtDataContextHolder.getInstance().getExtDataContextHolder().get();
        Long userId = moiraiUser.getUserId();
        if(extDataContext != null){
            List<String> keys = new ArrayList<>();
            keys.add(String.valueOf(userId));
            extDataContext.setKeys(keys);
        }

//        TransmittableThreadLocal<Map<String, String>> extContextHolder = ExtContextHolder.getInstance().getExtContextHolder();
//        if(extContextHolder != null){
//            Map<String,String> paramMap = new HashMap<>();
//            Long tenantId = moiraiUser.getTenantId();
//            logger.info("insertSelective tenantId = 【{}】",tenantId);
//            paramMap.put("tenantId",String.valueOf(tenantId));
//            extContextHolder.set(paramMap);
//        }
        return key;
    }

    /**
     * <B>方法名称：</B>更新用户信息<BR>
     * <B>概要说明：</B><BR>
     *
     * @return
     * @since 2020/3/3
     */
    @ExpDataFiled(TableName = "moirai_user",operType = OperType.UPDATE)
    public int updateByPrimaryKeySelective(MoiraiUser moiraiUser){
        int key = moiraiUserMapper.updateByPrimaryKeySelective(moiraiUser);
        ExtDataContext extDataContext = ExtDataContextHolder.getInstance().getExtDataContextHolder().get();
        Long userId = moiraiUser.getUserId();
        if(extDataContext != null){
            List<String> keys = new ArrayList<>();
            keys.add(String.valueOf(userId));
            extDataContext.setKeys(keys);
        }
        return key;
    }

    /**
     * <B>方法名称：</B>删除用户信息<BR>
     * <B>概要说明：</B><BR>
     *
     * @return
     * @since 2020/3/3
     */
    @ExpDataFiled(TableName = "moirai_user",operType = OperType.DELETE)
    public int deleteByPrimaryKey(Long userId){
        int key = moiraiUserMapper.deleteByPrimaryKey(userId);
        ExtDataContext extDataContext = ExtDataContextHolder.getInstance().getExtDataContextHolder().get();
        if(extDataContext != null){
            List<String> keys = new ArrayList<>();
            keys.add(String.valueOf(userId));
            extDataContext.setKeys(keys);
        }
        return key;
    }


    @ExpDataFiled(TableName = "moirai_user",operType = OperType.INSERT)
    public int addBatch(List<MoiraiUser> users){
        int key = moiraiUserMapper.addBatch(users);
        ExtDataContext extDataContext = ExtDataContextHolder.getInstance().getExtDataContextHolder().get();
        if(extDataContext != null){
            List<String> keys = new ArrayList<>();
            for(MoiraiUser moiraiUser : users){
                Long userId = moiraiUser.getUserId();
                keys.add(String.valueOf(userId));
            }
            extDataContext.setKeys(keys);
        }
        return key;
    }

    //获取用户详情  展示扩展字段
    @ExpDataFiled(TableName = "moirai_user",operType = OperType.SELECT)
    public MoiraiUser selectByPrimaryKey(Long userId){
        MoiraiUser moiraiUser = moiraiUserMapper.selectByPrimaryKey(userId);
        ExtDataContext extDataContext = ExtDataContextHolder.getInstance().getExtDataContextHolder().get();
        if(extDataContext != null){
            List<String> keys = new ArrayList<>();
            keys.add(String.valueOf(userId));
            extDataContext.setKeys(keys);
        }
        return moiraiUser;
    }

    @ExpDataFiled(TableName = "moirai_user",operType = OperType.SELECT)
    public List<MoiraiUser> findUserByCondition(MoiraiUserCondition moiraiUserCondition){
        List<MoiraiUser> moiraiUsers = moiraiUserMapper.findUserByCondition(moiraiUserCondition);
        ExtDataContext extDataContext = ExtDataContextHolder.getInstance().getExtDataContextHolder().get();
        if(extDataContext != null){
            List<String> keys = new ArrayList<>();
            for (MoiraiUser moiraiUser:moiraiUsers ) {
                keys.add(String.valueOf(moiraiUser.getUserId()));
            }
            extDataContext.setKeys(keys);
        }
        return moiraiUsers;
    }

    /**
     * <B>方法名称：</B>查询用户列表信息<BR>
     * <B>概要说明：</B><BR>
     *
     * @return
     * @since 2020/3/3
     */
    @ExpDataFiled(TableName = "moirai_user",operType = OperType.SELECT)
    public List<MoiraiUser> findUserListByCondition(MoiraiUserCondition moiraiUserCondition){
        PageHelper.startPage(moiraiUserCondition.getPageNo(), moiraiUserCondition.getPageSize());
        List<MoiraiUser> moiraiUsers = moiraiUserMapper.findUserListByCondition(moiraiUserCondition);
        ExtDataContext extDataContext = ExtDataContextHolder.getInstance().getExtDataContextHolder().get();
        if(extDataContext != null){
            List<String> keys = new ArrayList<>();
            for (MoiraiUser moiraiUser:moiraiUsers ) {
                keys.add(String.valueOf(moiraiUser.getUserId()));
            }
            extDataContext.setKeys(keys);
        }
        return moiraiUsers;
    }
}
