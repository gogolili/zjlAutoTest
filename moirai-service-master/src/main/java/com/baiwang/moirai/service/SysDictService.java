package com.baiwang.moirai.service;

import com.baiwang.moirai.model.sys.SysDict;
import com.baiwang.moirai.model.sys.SysDictCondition;
import com.baiwang.moirai.model.sys.SysDictDetail;
import com.baiwang.moirai.model.sys.TenantSysDict;

import java.util.List;
import java.util.Map;

public interface SysDictService {

    public Map<String,Object> queryDictInfo(SysDictCondition sysDictCondition);

    public void addDict(SysDict sysDict);

    public void updateDict(SysDict sysDict);

    public void deleteDict(SysDict sysDict);

    public Map<String,Object> getDictDetail(SysDictCondition sysDictCondition);

    public void addDictDetail(SysDictDetail sysDictDetail);

    public void updateDictDetail(SysDictDetail sysDictDetail);

    public void deleteDictDetail(SysDictDetail sysDictDetail);

    public void batchDelDetail(SysDictCondition condition);

    public SysDictDetail getOneDictDetailService(SysDictCondition condition);

    /**
     * 根据字典类型获取字典明细
     * @param dictType
     * @return
     */
    List<SysDict> getSysDictCacheByDictType(String dictType);

    /**
     * 清除字典明细缓存
     * @param dictType
     */
    void clearSysDictCache(String dictType);

    /**
     * 获取租户配置信息
     */
    List<SysDict> getTenantSysDict(TenantSysDict sysDict);
}
