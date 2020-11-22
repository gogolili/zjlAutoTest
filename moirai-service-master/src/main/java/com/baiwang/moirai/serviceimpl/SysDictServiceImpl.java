package com.baiwang.moirai.serviceimpl;

import com.alibaba.fastjson.JSONObject;
import com.baiwang.moirai.common.Constants;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.mapper.SysDictDetailMapper;
import com.baiwang.moirai.mapper.SysDictMapper;
import com.baiwang.moirai.model.sys.SysDict;
import com.baiwang.moirai.model.sys.SysDictCondition;
import com.baiwang.moirai.model.sys.SysDictDetail;
import com.baiwang.moirai.model.sys.TenantSysDict;
import com.baiwang.moirai.model.tenant.MoiraiTenantConfig;
import com.baiwang.moirai.service.MoiraiTenantConfigService;
import com.baiwang.moirai.service.SysDictService;
import com.baiwang.moirai.utils.RedisUtils;
import com.baiwang.moirai.utils.StrUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ContextConfiguration
public class SysDictServiceImpl implements SysDictService {


    private static final Logger logger = LoggerFactory.getLogger(SysDictServiceImpl.class);

    @Autowired
    private SysDictDetailMapper sysDictDetailMapper;

    @Autowired
    private SysDictMapper sysDictMapper;

    @Autowired
    private MoiraiTenantConfigService tenantConfigService;

    @Autowired
    private RedisUtils redisUtils;

    /**
     * 查询字典信息 包括分页+ 模糊查询
     *
     * @param sysDictCondition
     */
    public Map<String, Object> queryDictInfo(SysDictCondition sysDictCondition) {
        logger.info("查询字段信息 begin param = {}", JSONObject.toJSONString(sysDictCondition));
        PageHelper.startPage(sysDictCondition.getPageNo(), sysDictCondition.getPageSize());
        Map<String, Object> map = new HashMap<>();
        List<SysDict> sysDicts = sysDictMapper.selectByBean(sysDictCondition);
        if (sysDicts == null && sysDicts.size() <= 0) {
            return map;
        }
        PageInfo<SysDict> pageInfo = new PageInfo<>(sysDicts);
        Long total = pageInfo.getTotal();
        map.put("dictList", sysDicts);
        map.put("total", total);
        return map;
    }

    /**
     * 根据详情字段获取字典详情信息 包括分页+ 模糊查询
     *
     * @param sysDictCondition
     * @return
     */
    public Map<String, Object> getDetailByCondition(SysDictCondition sysDictCondition) {
        logger.info("查询字段信息 begin param = {}", JSONObject.toJSONString(sysDictCondition));
        PageHelper.startPage(sysDictCondition.getPageNo(), sysDictCondition.getPageSize());
        Map<String, Object> map = new HashMap<>();
        List<SysDictDetail> sysDicts = sysDictDetailMapper.selectByBean(sysDictCondition);
        if (sysDicts == null && sysDicts.size() <= 0) {
            return map;
        }
        PageInfo<SysDictDetail> pageInfo = new PageInfo<>(sysDicts);
        Long total = pageInfo.getTotal();
        map.put("dictDetailList", sysDicts);
        map.put("total", total);
        return map;
    }

    /**
     * 保存新增的字典信息
     *
     * @param sysDict
     */
    public void addDict(SysDict sysDict) {
        logger.info("add字段信息 begin param = {}", JSONObject.toJSONString(sysDict));
        SysDictCondition sysDictCondition = new SysDictCondition();
        sysDictCondition.setDictType(sysDict.getDictType());
        List<SysDict> sysDicts = sysDictMapper.selectByBean(sysDictCondition);
        if (sysDicts != null && sysDicts.size() > 0) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_DICT_MARK_REPEAT);
        }
        sysDictMapper.insert(sysDict);
    }

    /**
     * 根据主键更新字典信息
     *
     * @param sysDict
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateDict(SysDict sysDict) {
        logger.info("update字段信息 begin param = {}", JSONObject.toJSONString(sysDict));
        String dictType = sysDict.getDictType();
        if (!StrUtils.isEmpty(dictType)) {
            SysDictCondition sysDictCondition = new SysDictCondition();
            sysDictCondition.setDictType(sysDict.getDictType());
            List<SysDict> sysDicts = sysDictMapper.selectByBean(sysDictCondition);
            if (sysDicts != null && sysDicts.size() > 0) {
                SysDict sysDict1 = sysDicts.get(0);
                if (sysDict1.getId().longValue() != sysDict.getId().longValue()) {
                    throw new MoiraiException(MoiraiErrorEnum.MOIRAI_DICT_MARK_REPEAT);
                }
            }
        }
        sysDictMapper.updateByPrimaryKeySelective(sysDict);
        // 清除字典所有缓存
        clearSysDictCache(null);
    }

    /**
     * 根据主键删除字典信息
     *
     * @param sysDict
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteDict(SysDict sysDict) {
        logger.info("delete begin param = {}", JSONObject.toJSONString(sysDict));
        SysDictCondition condition = new SysDictCondition();
        condition.setId(sysDict.getId());
        List<SysDictDetail> sysDicts = sysDictDetailMapper.selectByBean(condition);
        if (sysDicts.size() > 0 && sysDicts != null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_DICT_REJECT_DELETE);
        }
        sysDictMapper.deleteByPrimaryKey(sysDict.getId());
        // 清除字典所有缓存
        clearSysDictCache(null);
    }

    /**
     * 根据字典id查询字典详情信息;根据字典详情条件获取字典详情信息 包括分页
     *
     * @param sysDictCondition
     * @return
     */
    public Map<String, Object> getDictDetail(SysDictCondition sysDictCondition) {
        logger.info("getDictDetail begin param = {}", JSONObject.toJSONString(sysDictCondition));
        PageHelper.startPage(sysDictCondition.getPageNo(), sysDictCondition.getPageSize());
        Map<String, Object> map = new HashMap<>();
        List<SysDictDetail> sysDicts = sysDictDetailMapper.selectByBean(sysDictCondition);
        if (sysDicts == null && sysDicts.size() <= 0) {
            return map;
        }
        PageInfo<SysDictDetail> pageInfo = new PageInfo<>(sysDicts);
        Long total = pageInfo.getTotal();
        map.put("dictList", sysDicts);
        map.put("total", total);

        return map;
    }

    /**
     * 插入字典详情信息
     *
     * @param sysDictDetail
     */
    public void addDictDetail(SysDictDetail sysDictDetail) {
        logger.info("addDictDetail begin param = {}", JSONObject.toJSONString(sysDictDetail));
        Long id = sysDictDetail.getId();
        SysDict sysDict = sysDictMapper.selectByPrimaryKey(id);
        if (sysDict == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_DICT_MARK_IS_NOT_EXIST);
        }
        SysDictCondition condition = new SysDictCondition();
        condition.setDictCode(sysDictDetail.getDictCode());
        condition.setId(id);
        List<SysDictDetail> sysDictDetails = sysDictDetailMapper.selectByBean(condition);
        if (sysDictDetails != null && sysDictDetails.size() > 0) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_DICT_CODE_REPEAT);
        }
        sysDictDetailMapper.insert(sysDictDetail);

    }

    /**
     * 更新字典详情信息
     *
     * @param sysDictDetail
     */
    public void updateDictDetail(SysDictDetail sysDictDetail) {
        logger.info("updateDictDetail begin param = {}", JSONObject.toJSONString(sysDictDetail));
        Long id = sysDictDetail.getId();
        if (id != null) {
            SysDict sysDict = sysDictMapper.selectByPrimaryKey(id);
            if (sysDict == null) {
                throw new MoiraiException(MoiraiErrorEnum.MOIRAI_DICT_MARK_IS_NOT_EXIST);
            }
        }
        String dictCode = sysDictDetail.getDictCode();
        if (!StrUtils.isEmpty(dictCode)) {
            SysDictCondition condition = new SysDictCondition();
            condition.setDictCode(sysDictDetail.getDictCode());
            condition.setId(id);
            List<SysDictDetail> sysDictDetails = sysDictDetailMapper.selectByBean(condition);
            if (sysDictDetails != null && sysDictDetails.size() > 0) {
                Long dictDetailId = sysDictDetails.get(0).getDictDetailId();
                Long dictDetailId1 = sysDictDetail.getDictDetailId();
                if (dictDetailId1.longValue() != dictDetailId.longValue()) {
                    throw new MoiraiException(MoiraiErrorEnum.MOIRAI_DICT_CODE_REPEAT);
                }
            }

        }
        sysDictDetailMapper.updateByPrimaryKeySelective(sysDictDetail);
        // 清除字典所有缓存
        clearSysDictCache(null);

    }

    /**
     * 根据主键id删除字典详情信息
     *
     * @param sysDictDetail
     */
    public void deleteDictDetail(SysDictDetail sysDictDetail) {
        sysDictDetailMapper.deleteByPrimaryKey(sysDictDetail.getDictDetailId());
        // 清除字典所有缓存
        clearSysDictCache(null);
    }

    /**
     * 根据主键id集合批量删除字典详情信息
     *
     * @param condition
     */
    public void batchDelDetail(SysDictCondition condition) {
        logger.info("batchDelDetail begin param = {}", JSONObject.toJSONString(condition));
        sysDictDetailMapper.batchDel(condition);
        // 清除字典所有缓存
        clearSysDictCache(null);
    }

    /**
     * 根据主键id获取详情
     *
     * @param condition
     */
    public SysDictDetail getOneDictDetailService(SysDictCondition condition) {
        logger.info("batchDelDetail begin param = {}", JSONObject.toJSONString(condition));
        List<SysDictDetail> sysDictDetails = sysDictDetailMapper.selectByBean(condition);
        if(sysDictDetails.size() >0 && sysDictDetails != null){
            return sysDictDetails.get(0);
        }
        return null;
    }

    /**
     * 根据字典类型获取字典明细（缓存）
     * @param dictType
     * @return
     */
    @Override
    public List<SysDict> getSysDictCacheByDictType(String dictType) {
        List<SysDict> list = redisUtils.getList(Constants.MOIRAI_SYS_DICT + dictType, SysDict.class);
        if (list != null) {
            return list;
        }
        SysDict sysDict = new SysDict();
        sysDict.setDictType(dictType);
        list = sysDictMapper.selectWholeDictInfo(sysDict);
        redisUtils.set(Constants.MOIRAI_SYS_DICT + dictType, list, 60 * 60 * 6);
        return list;
    }

    /**
     * 清除字典明细缓存
     * @param dictType
     */
    @Override
    public void clearSysDictCache(String dictType) {
        if (StringUtils.isBlank(dictType)) {
            redisUtils.delete(redisUtils.keys(Constants.MOIRAI_SYS_DICT + "*"));
        }else {
            redisUtils.delete(Constants.MOIRAI_SYS_DICT + dictType);
        }
    }

    /**
     * 获取租户字典配置信息
     */
    @Override
    public List<SysDict> getTenantSysDict(TenantSysDict tenantSysDict) {
        String dictType = tenantSysDict.getDictType();
        Long tenantId = tenantSysDict.getTenantId();
        List<SysDict> result = new ArrayList<>();
        List<MoiraiTenantConfig> list = tenantConfigService.getConfigListCache(tenantId, dictType);
        if (!list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getConfigCode().equals(dictType)) {
                    MoiraiTenantConfig config = list.get(i);
                    SysDict sysDict = new SysDict();
                    sysDict.setDictType(dictType);
                    sysDict.setId(config.getId());
                    sysDict.setDictDetailId(config.getId());
                    sysDict.setDictTypeName(config.getConfigName());
                    sysDict.setDictCode(config.getDetailCode());
                    sysDict.setDictName(config.getDetailName());
                    sysDict.setUseFlag(Constants.flag_Y.equals(config.getDelFlag()) ? Constants.flag_N : Constants.flag_Y);
                    result.add(sysDict);
                }
            }
        }
        if (result.isEmpty()) {
            result = getSysDictCacheByDictType(dictType);
        }
        return result;
    }
}
