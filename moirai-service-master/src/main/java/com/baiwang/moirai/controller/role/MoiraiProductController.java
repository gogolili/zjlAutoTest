package com.baiwang.moirai.controller.role;

import com.alibaba.fastjson.JSONObject;
import com.baiwang.moirai.annotation.UserCenterOperationLog;
import com.baiwang.moirai.api.MoiraiProductSvc;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.model.role.MoiraiProduct;
import com.baiwang.moirai.model.role.MoiraiProductCondition;
import com.baiwang.moirai.service.MoiraiProductService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 产品相关
 *
 * @author 程路超
 */
@RestController
@SuppressWarnings("all")
public class MoiraiProductController implements MoiraiProductSvc {
    private Logger logger = LoggerFactory.getLogger(MoiraiProductController.class);

    @Autowired
    private MoiraiProductService moiraiProductService;

    /**
     * <B>方法名称：</B>添加产品<BR>
     * <B>概要说明：</B>只是添加产品/应用<BR>
     *
     * @return
     * @since 2019年6月28日
     */
    @Override
    @UserCenterOperationLog(moduleName = "产品管理",action = "添加产品" ,description = "添加产品")
    public BWJsonResult addProduct(@RequestBody MoiraiProduct moiraiProduct) {
        if (moiraiProduct == null || StringUtils.isBlank(moiraiProduct.getCreater())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        moiraiProductService.addProduct(moiraiProduct);
        BWJsonResult bwJsonResult = new BWJsonResult();
        bwJsonResult.setMessage("添加产品成功");
        return bwJsonResult;
    }

    /**
     * <B>方法名称：</B>查询产品列表<BR>
     * <B>概要说明：</B>支持名称模糊查询<BR>
     *
     * @return
     * @since 2019年6月28日
     */
    @Override
    public BWJsonResult<MoiraiProduct> queryProductList(@RequestBody MoiraiProductCondition moiraiProductCondition) {
        //分页条数判断
        if (moiraiProductCondition.getPageNo() <= 0) {
            moiraiProductCondition.setPageNo(1);
        }
        if (moiraiProductCondition.getPageSize() <= 0) {
            moiraiProductCondition.setPageSize(10);
        }
        BWJsonResult<MoiraiProduct> bwJsonResult = moiraiProductService.queryProductList(moiraiProductCondition);
        return bwJsonResult;
    }

    /**
     * <B>方法名称：</B>查询产品层级<BR>
     * <B>概要说明：</B>运营后台审核使用<BR>
     *
     * @return
     * @since 2019年7月30日
     */
    @Override
    public BWJsonResult<MoiraiProduct> queryProductTree(@RequestBody MoiraiProduct moiraiProduct) {
        MoiraiProductCondition moiraiProductCondition = new MoiraiProductCondition();
        moiraiProductCondition.setSource("1");
        moiraiProductCondition.setPageSize(1000);
        BWJsonResult<MoiraiProduct> bwJsonResult = moiraiProductService.queryProductList(moiraiProductCondition);
        return bwJsonResult;
    }

    /**
     * <B>方法名称：</B>查询产品下拉列表<BR>
     * <B>概要说明：</B>用户添加应用时使用<BR>
     *
     * @return
     * @since 2019年7月01日
     */
    @Override
    public BWJsonResult<MoiraiProduct> queryParentProduct(@RequestBody MoiraiProduct moiraiProduct) {
        List<MoiraiProduct> productList = moiraiProductService.queryParentProduct(moiraiProduct);
        BWJsonResult<MoiraiProduct> bwJsonResult = new BWJsonResult<>(productList);
        bwJsonResult.setMessage("查询产品列表成功");
        return bwJsonResult;
    }

    /**
     * <B>方法名称：</B>查询产品详情<BR>
     * <B>概要说明：</B>编辑产品时回显<BR>
     *
     * @return
     * @since 2019年6月28日
     */
    @Override
    public BWJsonResult<MoiraiProduct> queryProductInfo(@RequestBody MoiraiProduct moiraiProduct) {
        if (moiraiProduct == null || moiraiProduct.getProductId() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        MoiraiProduct product = moiraiProductService.queryProductInfo(moiraiProduct);
        BWJsonResult<MoiraiProduct> bwJsonResult = new BWJsonResult<>(product);
        bwJsonResult.setMessage("查询产品详情成功");
        return bwJsonResult;
    }

    /**
     * <B>方法名称：</B>编辑产品保存<BR>
     * <B>概要说明：</B>保存修改<BR>
     *
     * @return
     * @since 2019年6月28日
     */
    @Override
    @UserCenterOperationLog(moduleName = "产品管理",action = "编辑产品" ,description = "编辑产品")
    public BWJsonResult editProduct(@RequestBody MoiraiProduct moiraiProduct) {
        if (moiraiProduct == null || moiraiProduct.getProductId() == null || StringUtils.isBlank(moiraiProduct.getModifyUser())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        moiraiProductService.editProduct(moiraiProduct);
        BWJsonResult bwJsonResult = new BWJsonResult();
        bwJsonResult.setMessage("编辑产品成功");
        return bwJsonResult;
    }

    /**
     * <B>方法名称：</B>产品相关删除<BR>
     * <B>概要说明：</B>删除应用时需检查是否有租户开通，如果存在已开通的租户，则提示不允许删除；删除产品时需要检查产品下是否有应用以及是否有租户开通产品<BR>
     *
     * @return
     * @since 2019年6月28日
     */
    @Override
    @UserCenterOperationLog(moduleName = "产品管理",action = "删除产品" ,description = "删除产品")
    public BWJsonResult delProduct(@RequestBody MoiraiProduct moiraiProduct) {
        if (moiraiProduct == null || moiraiProduct.getProductId() == null) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        moiraiProductService.delProduct(moiraiProduct);
        BWJsonResult bwJsonResult = new BWJsonResult();
        bwJsonResult.setMessage("删除产品成功");
        return bwJsonResult;
    }

    /**
     * <B>方法名称：</B>产品资源关系<BR>
     * <B>概要说明：</B>应用页面保存产品资源关系<BR>
     *
     * @return
     * @since 2019年7月19日
     */
    @Override
    public BWJsonResult addProductResource(@RequestBody MoiraiProduct moiraiProduct) {
        if (moiraiProduct == null || moiraiProduct.getProductId() == null ||
            moiraiProduct.getResourceList() == null || StringUtils.isBlank(moiraiProduct.getCreater())) {
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR);
        }
        moiraiProductService.addProductResource(moiraiProduct);
        BWJsonResult bwJsonResult = new BWJsonResult();
        bwJsonResult.setMessage("添加应用资源关联关系成功");
        return bwJsonResult;
    }

    /**
     * 查询产品及资源树（只包含产品，不包含应用）
     */
    @PostMapping("/queryProductResourceTree")
    public BWJsonResult<MoiraiProduct> queryProductResourceTree() {
        List<MoiraiProduct> list = moiraiProductService.queryProductResourceTree();
        return new BWJsonResult<>(list);
    }
}
