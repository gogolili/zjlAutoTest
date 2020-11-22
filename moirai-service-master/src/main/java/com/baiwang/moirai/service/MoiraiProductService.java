package com.baiwang.moirai.service;

import com.alibaba.fastjson.JSONObject;
import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.model.role.MoiraiProduct;
import com.baiwang.moirai.model.role.MoiraiProductCondition;
import java.util.List;

/**
 * @author 程路超
 */
public interface MoiraiProductService {

    int addProduct(MoiraiProduct moiraiProduct);

    BWJsonResult<MoiraiProduct> queryProductList(MoiraiProductCondition moiraiProductCondition);

    MoiraiProduct queryProductInfo(MoiraiProduct moiraiProduct);

    int editProduct(MoiraiProduct moiraiProduct);

    int delProduct(MoiraiProduct moiraiProduct);

    List<MoiraiProduct> queryParentProduct(MoiraiProduct moiraiProduct);

    int addProductResource(MoiraiProduct moiraiProduct);

    /**
     * 查询产品资源
     */
    List<MoiraiProduct> queryProductResourceTree();
}
