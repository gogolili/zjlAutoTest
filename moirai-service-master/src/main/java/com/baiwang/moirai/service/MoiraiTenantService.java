/*
 * @项目名称: Moirai
 * @文件名称: MoiraiTenantService.java
 * @Date: 17-11-4 下午2:56
 * @author Lance cui
 *
 */

package com.baiwang.moirai.service;

import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.tenant.MoiraiTenant;
import com.baiwang.moirai.model.tenant.MoiraiTenantListCondition;
import com.baiwang.moirai.model.tenant.MoiraiTenantListVO;
import com.baiwang.moirai.model.tenant.MoiraiTenantVO;
import com.baiwang.moirai.model.user.MoiraiUser;
import java.util.List;

public interface MoiraiTenantService {

    /**
     * 查询租户的状态
     */
    int tenantRegisterStatus(String tenantName, String taxCode);

    /**
     * 删除僵尸租户
     */
    int deleteZombieTenant(String tenantName, String taxCode);

//    MoiraiUser queryAccountByEmail(String tenantEmail);

    /**
     * 前台注册租户
     */
    MoiraiUser regTenant(MoiraiTenantVO moiraiTenant);

    /**
     * 后台添加租户
     */
    MoiraiUser addTenant(MoiraiTenantVO moiraiTenant);

    /**
     * 完善租户信息，包含更新租户和组织机构信息
     */
    int completeTenantInfo(MoiraiTenantVO moiraiTenant);

    /**
     * 更新租户信息
     */
    int updateTenant(MoiraiTenant moiraiTenant);

    /**
     * 获取租户列表
     */
    BWJsonResult<MoiraiTenantListVO> getTenantListPage(MoiraiTenantListCondition tenantListCondition);

    /**
     * 获取租户列表 单表查询
     */
    BWJsonResult<MoiraiTenantListVO> getTenantList2Page(MoiraiTenantListCondition tenantListCondition);

    /**
     * 获取僵尸租户列表
     */
    BWJsonResult<MoiraiTenantListVO> getTenantZomListPage(MoiraiTenantListCondition tenantListCondition);

    /**
     * 按条件获取租户
     */
    List<MoiraiTenantVO> getTenantByCondition(MoiraiTenantVO moiraiTenantVO);

    int deleteTenant(Long tenantId);

    MoiraiTenantVO getCacheMoiraiTenant(Long tenantId);

    MoiraiTenantVO queryTenantByTaxCode(MoiraiOrg moiraiOrg);

    void openTenantProduct(MoiraiOrg moiraiOrg);

    /**
     * 获取当前登录租户信息
     */
    MoiraiTenantVO getCurrentLoginTenantInfo();

    /**
     * 更新租户资质文件
     */
    void updateQualiFilepath(MoiraiTenant tenant);
}
