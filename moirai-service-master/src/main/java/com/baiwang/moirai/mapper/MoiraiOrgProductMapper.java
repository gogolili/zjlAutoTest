package com.baiwang.moirai.mapper;

import com.baiwang.moirai.model.org.MoiraiOrgProduct;
import com.baiwang.moirai.model.org.MoiraiOrgProductVO;
import com.baiwang.moirai.model.tenant.MoiraiTenant;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

@Service
@Mapper
public interface MoiraiOrgProductMapper {

    /** 组织机构开通产品 **/
    int insertSelective(MoiraiOrgProduct record);

    int updateByPrimaryKeySelective(MoiraiOrgProduct record);

    /** 删除组织机构开通的所有产品 **/
    int deleteOrgAllProducts(Long orgId);

    /** 查询租户的所有产品 不去重 **/
    List<MoiraiOrgProduct> queryTenantProducts(Long tenantId);

    /** 批量查询机构开通的产品 **/
    List<MoiraiOrgProduct> queryOrgProducts(List<Long> orgIdList);

    /** 查询组织机构中所有税号开通的所有产品-含税号 **/
    List<MoiraiOrgProductVO> queryTenantAllProducts(MoiraiTenant moiraiTenant);

    /** 查询组织机构的所有产品 **/
    List<MoiraiOrgProduct> selectOrgProducts(@Param("orgId") Long orgId, @Param("productId") Long productId);

    /** 判断是否有组织机构使用产品 */
    List<MoiraiOrgProduct> queryOrgByProduct(Long productId);

    /** 导入机构批量添加机构产品 */
    int addOrgProductList(List<MoiraiOrgProduct> record);

    List<MoiraiOrgProduct> findOrgProductByCondition(MoiraiOrgProduct product);

    /** 组织机构删除开通的产品 **/
    int deleteOrgProduct(@Param("productId") Long productId, @Param("orgId") Long orgId);

    List<MoiraiOrgProduct> selectCPProductsByTenantId(@Param("tenantId") Long tenantId);

    List<MoiraiOrgProduct> selectCPProductsByOrgId(@Param("orgId") Long orgId);

}