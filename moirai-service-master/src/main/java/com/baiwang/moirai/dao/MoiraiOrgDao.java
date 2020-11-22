package com.baiwang.moirai.dao;

import com.baiwang.cloud.common.aop.aspect.ExpDataFiled;
import com.baiwang.cloud.common.context.ExtDataContext;
import com.baiwang.cloud.common.context.ExtDataContextHolder;
import com.baiwang.cloud.common.context.OperType;
import com.baiwang.moirai.mapper.MoiraiOrgMapper;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.org.MoiraiOrgCondition;
import com.github.pagehelper.PageHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MoiraiOrgDao {

    private static Logger logger = LoggerFactory.getLogger(MoiraiOrgDao.class);

    @Autowired
    MoiraiOrgMapper moiraiOrgMapper;


    @ExpDataFiled(TableName = "moirai_org",operType = OperType.SELECT)
    public MoiraiOrg selectOneOrg(MoiraiOrg moiraiOrg){
        MoiraiOrg moiraiOrg2 = moiraiOrgMapper.selectOneOrg(moiraiOrg);
        ExtDataContext extDataContext = ExtDataContextHolder.getInstance().getExtDataContextHolder().get();
        if(extDataContext != null){
            List<String> keys = new ArrayList<>();
            keys.add(String.valueOf(moiraiOrg.getOrgId()));
            extDataContext.setKeys(keys);
        }
        return moiraiOrg2;

    }

    @ExpDataFiled(TableName = "moirai_org",operType = OperType.INSERT)
    public int addNoTaxOrg(MoiraiOrg moiraiOrg){
        int i = moiraiOrgMapper.addNoTaxOrg(moiraiOrg);
        ExtDataContext extDataContext = ExtDataContextHolder.getInstance().getExtDataContextHolder().get();
        Long orgId = moiraiOrg.getOrgId();
        if(extDataContext != null){
            List<String> keys = new ArrayList<>();
            keys.add(String.valueOf(orgId));
            extDataContext.setKeys(keys);
        }
        return i;
    }

    @ExpDataFiled(TableName = "moirai_org",operType = OperType.INSERT)
    public int addOrgList(List<MoiraiOrg> moiraiOrgs){
        int i = moiraiOrgMapper.addOrgList(moiraiOrgs);
        ExtDataContext extDataContext = ExtDataContextHolder.getInstance().getExtDataContextHolder().get();
        if(extDataContext != null){
            List<String> keys = new ArrayList<>();
            for(MoiraiOrg moiraiOrg:moiraiOrgs) {
                Long orgId = moiraiOrg.getOrgId();
                keys.add(String.valueOf(orgId));
            }
            extDataContext.setKeys(keys);
        }
        return i;
    }

    @ExpDataFiled(TableName = "moirai_org",operType = OperType.INSERT)
    public int addTaxOrg(MoiraiOrg moiraiOrg){
        int i = moiraiOrgMapper.addTaxOrg(moiraiOrg);
        ExtDataContext extDataContext = ExtDataContextHolder.getInstance().getExtDataContextHolder().get();
        Long orgId = moiraiOrg.getOrgId();
        if(extDataContext != null){
            List<String> keys = new ArrayList<>();
            keys.add(String.valueOf(orgId));
            extDataContext.setKeys(keys);
        }
        return i;
    }

    @ExpDataFiled(TableName = "moirai_org",operType = OperType.UPDATE)
    public int updateOrg(MoiraiOrg moiraiOrg){
        int i = moiraiOrgMapper.updateOrg(moiraiOrg);
        ExtDataContext extDataContext = ExtDataContextHolder.getInstance().getExtDataContextHolder().get();
        Long orgId = moiraiOrg.getOrgId();
        if(extDataContext != null){
            List<String> keys = new ArrayList<>();
            keys.add(String.valueOf(orgId));
            extDataContext.setKeys(keys);
        }
        return i;
    }


    @ExpDataFiled(TableName = "moirai_org",operType = OperType.DELETE)
    public int deleteOrg(Long orgId){
        int i = moiraiOrgMapper.deleteOrg(orgId);
        ExtDataContext extDataContext = ExtDataContextHolder.getInstance().getExtDataContextHolder().get();
        if(extDataContext != null){
            List<String> keys = new ArrayList<>();
            keys.add(String.valueOf(orgId));
            extDataContext.setKeys(keys);
        }
        return i;
    }

    @ExpDataFiled(TableName = "moirai_org",operType = OperType.SELECT)
    public List<MoiraiOrg> selectOrgBatcher(MoiraiOrgCondition condition){
        List<MoiraiOrg> moiraiOrgs = moiraiOrgMapper.selectOrgBatcher(condition);
        ExtDataContext extDataContext = ExtDataContextHolder.getInstance().getExtDataContextHolder().get();
        if(extDataContext != null){
            List<String> keys = new ArrayList<>();
            List<String> orgIds = moiraiOrgs.stream().map(item -> item.getOrgId().toString()).collect(Collectors.toList());
            keys.addAll(orgIds);
            extDataContext.setKeys(keys);
        }
        return moiraiOrgs;
    }

    @ExpDataFiled(TableName = "moirai_org",operType = OperType.SELECT)
    public List<MoiraiOrg> selectOrgByAuth(MoiraiOrgCondition condition){
        List<MoiraiOrg> moiraiOrgs = moiraiOrgMapper.selectOrgByAuth(condition);
        ExtDataContext extDataContext = ExtDataContextHolder.getInstance().getExtDataContextHolder().get();
        if(extDataContext != null){
            List<String> keys = new ArrayList<>();
            List<String> orgIds = moiraiOrgs.stream().map(item -> item.getOrgId().toString()).collect(Collectors.toList());
            keys.addAll(orgIds);
            extDataContext.setKeys(keys);
        }
        return moiraiOrgs;
    }

    @ExpDataFiled(TableName = "moirai_org",operType = OperType.SELECT)
    public List<MoiraiOrg> queryOrgByCondition(MoiraiOrgCondition condition){
        PageHelper.startPage(condition.getPageNo(), condition.getPageSize());
        List<MoiraiOrg> moiraiOrgs = moiraiOrgMapper.queryOrgByCondition(condition);
        ExtDataContext extDataContext = ExtDataContextHolder.getInstance().getExtDataContextHolder().get();
        if(extDataContext != null){
            List<String> keys = new ArrayList<>();
            List<String> orgIds = moiraiOrgs.stream().map(item -> item.getOrgId().toString()).collect(Collectors.toList());
            keys.addAll(orgIds);
            extDataContext.setKeys(keys);
        }
        return moiraiOrgs;
    }
}
