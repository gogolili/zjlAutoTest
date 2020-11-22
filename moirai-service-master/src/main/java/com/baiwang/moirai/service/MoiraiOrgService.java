/*
 * @项目名称: Moirai
 * @文件名称: MoiraiOrgService.java
 * @Date: 17-11-7 下午6:08
 * @author Lance cui
 *
 */

package com.baiwang.moirai.service;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.org.MoiraiOrgCondition;
import com.baiwang.moirai.model.org.MoiraiOrgConfig;
import com.baiwang.moirai.model.org.MoiraiOrgProduct;
import com.baiwang.moirai.model.org.MoiraiOrgProductVO;
import com.baiwang.moirai.model.role.MoiraiUserAuthz;
import com.baiwang.moirai.model.tenant.MoiraiTenant;
import com.baiwang.moirai.model.user.MoiraiUser;
import java.util.List;
import java.util.Map;

public interface MoiraiOrgService {

    MoiraiOrg combineOrgTree(List<MoiraiOrg> tenantOrgList, Long topOrgId);

    MoiraiUser addorg(MoiraiOrg moiraiOrg);

    /**
     * 验证添加机构时机构编码是否重复
     **/
    MoiraiOrg validOrgCode(MoiraiOrg moiraiOrg);

    /**
     * 根据组织机构ID查询组织机构
     **/
    MoiraiOrg selectByOrgId(Long parentOrgId);

    /**
     * 返回唯一的组织机构
     **/
    MoiraiOrg selectOneOrg(MoiraiOrg moiraiOrg);

    /**
     * 根据组织机构税号查询组织机构
     **/
    MoiraiOrg queryByTaxCode(String taxCode);

    /**
     * 根据组织机构名称查询组织机构
     **/
    List<MoiraiOrg> queryByOrgName(String orgName);

    /**
     * 更新组织机构
     **/
    int updateOrg(MoiraiOrg moiraiOrg);

    /**
     * 修改父级或者非纳税改纳税
     */
    void updateOrgEntity(MoiraiOrg moiraiOrg);

    /**
     * 查询租户的组织机构树
     * <p>
     * 1-返回树形结构 2-返回list结构
     **/
    List<MoiraiOrg> getTenantOrgTree(MoiraiOrg moiraiOrg, int struc);

    /**
     * 查询租户的组织机构简单展示树
     * <p>
     * 1-返回树形结构 2-返回list结构
     **/
    List<MoiraiOrg> getTenantOrgSimpleTree(MoiraiOrg moiraiOrg, int struc);

    /**
     * 获取组织机构的下级子机构
     **/
    List<MoiraiOrg> getOrgChildren(Long orgId);

    /**
     * 删除组织机构开通的所有产品
     **/
    int deleteOrgProducts(Long orgId);

    /**
     * 组织机构开通产品
     **/
    int addOrgProduct(MoiraiOrgProduct moiraiOrgProduct);

    /**
     * 组织机构批量开通产品,haDefault=true添加默认产品
     **/
    int addOrgProducts(MoiraiOrg moiraiOrg, boolean haDefault, Long qdBm);

    /**
     * 设置组织机构相关配置
     **/
    BWJsonResult setOrgConfig(List<MoiraiOrgConfig> moiraiOrgConfigList);

    /**
     * 查询组织机构相关配置
     **/
    MoiraiOrgConfig getOrgConfig(MoiraiOrgConfig moiraiOrgConfig);

    /**
     * 查询组织机构所有相关配置
     **/
    List<MoiraiOrgConfig> getAllOrgConfig(MoiraiOrgConfig moiraiOrgConfig);

    /**
     * 查询组织机构所有产品
     **/
    List<MoiraiOrgProduct> getOrgProducts(Long orgId);

    /**
     * 租户列表中的详细信息-租户相关页面
     **/
    List<MoiraiOrgProductVO> getTenantAllProducts(MoiraiTenant moiraiTenant);

    /**
     * 纳税主体列表，组织机构列表
     **/
    BWJsonResult<MoiraiOrg> getOrgListPage(MoiraiOrgCondition moiraiOrgCondition);

    /** 纳税主体列表，组织机构列表 **/
    BWJsonResult<MoiraiOrg> getOrgListPage2(MoiraiOrgCondition moiraiOrgCondition);

    BWJsonResult<MoiraiOrg> getOrgByCondition(MoiraiOrgCondition moiraiOrg);

    /**
     * 批量查询组织机构
     */
    List<MoiraiOrg> getOrgListByCondition(Map<String, List> moiraiOrgList);

    /**
     * 更新组织机构税号，更新组织机构名称,并做历史记录
     */
    int updateOrgTaxName(MoiraiOrg moiraiOrg);

    /**
     * 根据组织机构历史记录查询组织机构
     */
    List<MoiraiOrg> getOrgHistoryTaxOrName(MoiraiOrg moiraiOrg);

    /**
     * 非纳税主体时，获取上级的纳税主体组织机构
     */
    MoiraiOrg getOrgTaxEntity(MoiraiOrg moiraiOrg);

    /**
     * 非纳税主体时，获取上级的纳税主体组织机构
     */
    public MoiraiOrg getOrgFatherInfo(Long orgId);

    /**
     * 验证纳税主体必填字段
     */
    public void validateParam(MoiraiOrg moiraiOrg);

    /**
     * 验证纳税主体税号
     */
    public boolean cheackTaxCodeStatus(MoiraiOrg moiraiOrg);

    /**
     * 删除组织机构时，判断删除相关信息
     */
    void deleteOrgRel(MoiraiOrg moiraiOrg);

    /**
     * 进入销项时判断机构信息是否完整
     */
    BWJsonResult<MoiraiOrg> checkOrgInfo(MoiraiOrg moiraiOrg);

    /**
     * 验证纳主体第二页信息
     */
    void validatetaxOrg(MoiraiOrg moiraiOrg);

    /**
     * 验证纳税信息
     */
    void validateTaxmsg(MoiraiOrg moiraiOrg);

    /**
     * 验证机构名称和机构代码
     *
     * @param moiraiOrg
     */
    void validateOrgNameAndCode(MoiraiOrg moiraiOrg);

    /**
     * 是否开通bi
     *
     * @param moiraiOrg
     * @return
     */
    MoiraiOrgProduct addIncidentalProduct(MoiraiOrg moiraiOrg);

    List<MoiraiUserAuthz> getUserAuthBycondition(Long userId, Long resourceId);

    BWJsonResult<List<Map<String, Object>>> findTaxCodeListInfo(Map<String, Object> map);

    /**
     * 同步租户下所有开通CP产品的机构
     *
     * @param tenantId
     */
    void syncOrgs(Long tenantId);

    MoiraiTenant checkAddOrgParam(MoiraiOrg moiraiOrg);

    MoiraiOrg getCacheMoiraiOrg(Long orgId, String taxCode);

    void deleteCacheOrg(Long orgId, String taxCode);

    void combineTreeChildren(MoiraiOrg moiraiOrg, Map<Long, List<MoiraiOrg>> tenantOrgMap);

    MoiraiOrg getOrgByOrgId(MoiraiOrg moiraiOrg);

    int gainAuthByProductId(List<Long> products, MoiraiUser user);

    /**
     * 查询组织机构信息
     */
    BWJsonResult<MoiraiOrg> getChannelOrgByCondition(MoiraiOrgCondition moiraiOrg);

    void openOrgProduct(MoiraiOrg moiraiOrg);
}
