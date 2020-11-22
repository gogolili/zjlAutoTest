package com.baiwang.moirai.controller.dict;

import com.baiwang.moirai.api.SysDictSvc;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.model.sys.SysDict;
import com.baiwang.moirai.model.sys.SysDictCondition;
import com.baiwang.moirai.model.sys.SysDictDetail;
import com.baiwang.moirai.service.SysDictService;
import com.baiwang.moirai.utils.StrUtils;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SysDictController implements SysDictSvc {

    @Autowired
    private SysDictService sysDictService;

    /**
     * 查询字段信息：分页 + 模糊查询（dictTypeName）
     *
     * @param sysDictCondition
     * @return
     */
    public BWJsonResult<SysDict> queryDictInfo(@RequestBody SysDictCondition sysDictCondition) {
        if (sysDictCondition.getPageSize() == 0) {
            sysDictCondition.setPageSize(10);
        }
        Map<String, Object> rtnMap = sysDictService.queryDictInfo(sysDictCondition);
        List<SysDict> list = (List<SysDict>) rtnMap.get("dictList");
        BWJsonResult<SysDict> bwJsonResult = new BWJsonResult(list);
        Long total = (Long) rtnMap.get("total");
        bwJsonResult.setTotal(total.intValue());
        return bwJsonResult;
    }

    /**
     * 添加字典信息
     *
     * @param sysDict
     * @return
     */
    public BWJsonResult<SysDict> addDict(@RequestBody SysDict sysDict) {
        if (StrUtils.isEmpty(sysDict.getDictType()) || StrUtils.isEmpty(sysDict.getDictTypeName()) || StrUtils.isEmpty(sysDict.getUseType())) {
            return new BWJsonResult(new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        sysDictService.addDict(sysDict);
        BWJsonResult<SysDict> bwJsonResult = new BWJsonResult();
        bwJsonResult.setMessage("字典信息添加成功!");
        return bwJsonResult;
    }

    /**
     * 根据主键更新字典信息
     *
     * @param sysDict
     * @return
     */
    public BWJsonResult updateDict(@RequestBody SysDict sysDict) {
        if (StrUtils.isEmpty(sysDict.getId() + "")) {
            return new BWJsonResult(new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        sysDictService.updateDict(sysDict);
        BWJsonResult bwJsonResult = new BWJsonResult();
        bwJsonResult.setMessage("字典信息更新成功!");
        return bwJsonResult;
    }

    /**
     * 删除字典信息
     *
     * @param sysDict
     * @return
     */
    public BWJsonResult deleteDict(@RequestBody SysDict sysDict) {
        if (StrUtils.isEmpty(sysDict.getId() + "")) {
            return new BWJsonResult(new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
        }
        sysDictService.deleteDict(sysDict);
        BWJsonResult bwJsonResult = new BWJsonResult();
        bwJsonResult.setMessage("字典信息删除成功!");
        return bwJsonResult;
    }

    /**
     * 根据字典id查询字典详情信息;根据字典详情条件获取字典详情信息 包括分页
     *
     * @param sysDictCondition
     * @return
     */
    public BWJsonResult<SysDictDetail> getDictDetail(@RequestBody SysDictCondition sysDictCondition) {
//        if(StrUtils.isEmpty(sysDictCondition.getId()+"")){
//            return new BWJsonResult(new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
//        }
        if (sysDictCondition.getPageSize() == 0) {
            sysDictCondition.setPageSize(10);
        }
        Map<String, Object> rtnMap = sysDictService.getDictDetail(sysDictCondition);
        List<SysDictDetail> list = (List<SysDictDetail>) rtnMap.get("dictList");
        BWJsonResult<SysDictDetail> bwJsonResult = new BWJsonResult(list);
        Long total = (Long) rtnMap.get("total");
        bwJsonResult.setTotal(total.intValue());
        bwJsonResult.setMessage("字典详情信息查询成功!");
        return bwJsonResult;
    }

    /**
     * 插入字典详情信息
     *
     * @param sysDictCondition
     * @return
     */
    public BWJsonResult addDictDetail(@RequestBody SysDictDetail sysDictCondition) {

        if (StrUtils.isEmpty(sysDictCondition.getId() + "") || StrUtils.isEmpty(sysDictCondition.getOrderIndex() + "")) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        sysDictService.addDictDetail(sysDictCondition);
        BWJsonResult<SysDictDetail> bwJsonResult = new BWJsonResult();
        bwJsonResult.setMessage("字典详情信息实例化成功!");
        return bwJsonResult;
    }

    /**
     * 更新字典详情信息
     *
     * @param sysDictDetail
     * @return
     */
    public BWJsonResult updateDictDetail(@RequestBody SysDictDetail sysDictDetail) {

        if (StrUtils.isEmpty(sysDictDetail.getDictDetailId() + "")) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        sysDictService.updateDictDetail(sysDictDetail);
        BWJsonResult<SysDictDetail> bwJsonResult = new BWJsonResult();
        bwJsonResult.setMessage("字典详情信息更新成功!");
        return bwJsonResult;
    }

    /**
     * 根据主键id删除详情信息
     *
     * @param sysDictDetail
     * @return
     */
    public BWJsonResult deleteDictDetail(@RequestBody SysDictDetail sysDictDetail) {
        if (StrUtils.isEmpty(sysDictDetail.getDictDetailId() + "")) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        sysDictService.deleteDictDetail(sysDictDetail);
        BWJsonResult<SysDictDetail> bwJsonResult = new BWJsonResult();
        bwJsonResult.setMessage("字典详情信息删除成功!");
        return bwJsonResult;
    }

    /**
     * 根据主键id集合批量删除字典详情信息
     *
     * @param condition
     * @return
     */
    public BWJsonResult batchDelDetail(@RequestBody SysDictCondition condition) {
        if (condition.getList() == null || condition.getList().size() <= 0) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        sysDictService.batchDelDetail(condition);
        BWJsonResult<SysDictDetail> bwJsonResult = new BWJsonResult();
        bwJsonResult.setMessage("字典详情信息批量删除成功!");
        return bwJsonResult;
    }

    /**
     * 根据主键id获取详情信息
     *
     * @param condition
     * @return
     */
    public BWJsonResult getOneDictDetail(@RequestBody SysDictCondition condition) {
        if (StrUtils.isEmpty(condition.getDictDetailId() + "")) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        SysDictDetail oneDictDetailService = sysDictService.getOneDictDetailService(condition);
        return new BWJsonResult(oneDictDetailService);
    }
}
