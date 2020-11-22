package com.baiwang.moirai.mapper;
/**
 *
 */

import com.baiwang.moirai.model.role.MoiraiProduct;
import com.baiwang.moirai.model.role.MoiraiProductCondition;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

@Mapper
@Service
public interface MoiraiProductMapper {
    List<MoiraiProduct> selectOrgProductDetail(MoiraiProductCondition moiraiProductCondition);

    /**
     * 删除
     * @param productId
     * @return
     */
    int deleteByPrimaryKey(Long productId);

    /**
     * 选着插入
     * @param record
     * @return
     */
    int insertSelective(MoiraiProduct record);

    /**
     * id查询
     * @param productId
     * @return
     */
    MoiraiProduct selectByPrimaryKey(Long productId);

    /**
     * 条件查询
     * @param moiraiProduct
     * @return
     */
    List<MoiraiProduct> selectByBean(MoiraiProduct moiraiProduct);

    /**insert
     * 选着更新
     * @param record
     * @return
     */
    int updateByPrimaryKeySelective(MoiraiProduct record);

    /**
     * 根据resource查询产品
     * @param list
     * @return
     */
    List<MoiraiProduct> selectBatchInfo(List<Long> list);

}