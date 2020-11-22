package com.baiwang.moirai.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Constants {

    public static final String sdf_pattern = "yyyy-MM-dd HH:mm:ss";

    public static final String flag_N = "N";
    public static final String flag_L = "L";//锁定状态
    public static final String flag_Y = "Y";

    public static final String ZH_INITUSER_PASSWORD = "123456";

    /**
     * 用户表
     */
    public final static String MOIRAI_USER = "MOIRAI_USER";
    /**
     * 用户信息表
     */
    public final static String MOIRAI_USERINFO = "MOIRAI_USERINFO";
    /**
     * 用户历史密码表
     */
    public final static String MOIRAI_USER_HISTORY_PASSWORD = "moirai_user_history_password";
    /**
     * 角色信息表
     */
    public final static String MOIRAI_ROLE = "MOIRAI_ROLE";
    /**
     * 角色资源表
     */
    public final static String MOIRAI_ROLE_RESOURCE = "MOIRAI_ROLE_RESOURCE";
    /**
     * 资源信息表
     */
    public final static String MOIRAI_RESOURCE = "MOIRAI_RESOURCE";
    /**
     * 资源规则表
     */
    public final static String MOIRAI_RESOURCE_SECURITY = "MOIRAI_RESOURCE_SECURITY";
    /**
     * 规则表
     */
    public final static String MOIRAI_SECURITY_CONTROL = "MOIRAI_SECURITY_CONTROL";
    /**
     * 产品信息表
     */
    public final static String MOIRAI_PRODUCT = "MOIRAI_PRODUCT";
    /**
     * 产品资源表
     */
    public final static String MOIRAI_PRODUCT_RESOURCE = "MOIRAI_PRODUCT_RESOURCE";

    /**
     * 租户表
     */
    public final static String MOIRAI_TENANT = "MOIRAI_TENANT";

    /**
     * 渠道租户表
     */
    public final static String MOIRAI_CHANNEL_TENANT = "MOIRAI_CHANNEL_TENANT";

    /**
     * 组织机构表
     */
    public final static String MOIRAI_ORG = "MOIRAI_ORG";

    /**
     * 组织机构产品表
     */
    public final static String MOIRAI_ORG_PRODUCT = "MOIRAI_ORG_PRODUCT";

    /**
     * 组织机构配置信息表
     */
    public final static String MOIRAI_ORG_CONFIG = "MOIRAI_ORG_CONFIG";

    public final static String MOIRAI_ORG_HISTORY = "MOIRAI_ORG_HISTORY";

    /**
     * 用户登录历史表
     */
    public final static String MOIRAI_USER_LOGIN_HISTORY = "MOIRAI_USER_LOGIN_HISTORY";
    /**
     * C端用户B端用户关系表
     */
    public final static String MOIRAI_USER_MEMBER = "moirai_user_member";

    public final static String MOIRAI_PWD_KEY = "moirai:smsCert:";

    public final static String MOIRAI_PWD_SMSCODE = "moirai:smsCode:";

    public final static String MOIRAI_KEY_PAIR = "moirai:keyPair:";

    public final static String MOIRAI_USER_ERRORFILE = "moirai:moiraiUser:";

    public final static String MOIRAI_ORG_ERRORFILE = "moirai:moiraiOrg:";

    public final static String MOIRAI_ORG_INFO = "moirai:moiraiOrgInfo:";

    public final static String MOIRAI_TENANT_INFO = "moirai:moiraiTenantInfo:";

    public final static String MOIRAI_TENANT_CONFIG = "moirai:moiraiTenantConfig:";

    public final static String MOIRAI_SYS_DICT = "moirai:moiraiSysDict:";

    /**
     * 纳税主体
     */
    public final static String MOIRAI_IS_TAXER = "1";

    public final static String MOIRAI_IS_NOT_TAXER = "2";

    /**
     * 设备类型
     */
    public final static String MOIRAI_DEVICE_DEFAULT = "100";

    public final static String MOIRAI_DEVICE_SERVER = "0";

    public final static String MOIRAI_DEVICE_PANEL = "1";

    public final static String MOIRAI_DEVICE_TAXBOX = "3";

    public final static String MOIRAI_DEVICE_VIRTUAL_UKEY = "4";//虚拟UKEY，等同于密码机

    public final static String MOIRAI_DEVICE_TAX_UKEY = "5";//税务UKEY

    public final static String MOIRAI_DEVICE_GOLD_PANEL = "7";

    /**
     * 托管方式
     */
    public final static String MOIRAI_TGTYPE_PT = "0";//平台托管

    public final static String MOIRAI_TGTYPE_ZC = "1";//自持或者自建

    /**
     * 自行管理方式
     */
    public final static String MOIRAI_SELFMANAGER_FALSE = "0";//自行管理：否

    public final static String MOIRAI_SELFMANAGER_TRUE = "1";//自行管理：是

    /**
     * 二期用户中心 机构角色分配表
     */
    public final static String MOIRAI_USER_AUTHZ = "moirai_user_authz";

    public final static String MOIRAI_USER_DATA_SCOPE = "moirai_user_data_scope";

    /**
     * 用户登录错误次数
     */
    public static final String LOGINRETRYCOUNT = "authSSO:loginRetryCount:";
    /**
     * 调用bwapi接口
     */
    //弃用，调用推送中心
    //public final static String MOIRAI_SMS_SEND = "bwsmssend?Format=json";

    //弃用，调用推送中心
    //public final static String MOIRAI_SMS_CHECK = "bwsmscheck?Format=json";

    public final static String TENANTID_OPENCLOUD = "/user/insertBatchTenant";

    public final static String TAXCODE_OPENCLOUD = "/user/insertBatchUser";

    public final static String USER_OPENCLOUD = "/api/service/duplicateCheck";

    public final static String REMOVE_TAXCODE_OPENCLOUD = "/user/updateuser?status=2&nsrsbh=";

    /**
     * excel导入相关
     */
    public final static String ERRORFILE_LOCATION = "/Users/zhangxianchao";

    public final static int ORGEXCEL_MAXCOUNT = 2000;

    public final static String ORGEXCEL_FILENAME = "/errorOrgInfo.txt";

    public final static int USEREXCEL_MAXCOUNT = 1000;

    public final static String USEREXCEL_FILENAME = "/errorUserInfo.txt";

    public final static Long BI_PRODUCT = 24L;

    public final static String JRD_CODE = "123456";

    /**
     * E发票相关
     */
    public final static Long MOIRAI_PRODUCT_TYPE = 1L;

    public final static Long MOIRAI_PRODUCT_OPEN_TYPE = 1L;

    /**
     * 渠道相关
     */
    //白望直销
    public final static Long DEFAULT_QDBM = 2L;
    //移动开票
    public final static Long MOBILE_INVOICE_PRODUCT = 2L;

    public final static Integer DEFAULT_PRODUCT = 10;

    /** 操作：0:启用 1:禁用 2:批量删除用户 3:重置密码 4:已授权列表 5:未授权列表 6:解绑用户
     * (开放平台B端账号使用7:校验账号是否在传入税号所在租户下 8:校验账号是否在传入税号所在机构下)**/
    public final static String MOIRAI_USER_OPEN = "0";
    public final static String MOIRAI_USER_CLOSE = "1";
    public final static String MOIRAI_USER_DELETES = "2";
    public final static String MOIRAI_USER_RESETPASSWORD = "3";
    public final static String MOIRAI_USER_AUTHLIST = "4";
    public final static String MOIRAI_USER_UNAUTHLIST = "5";
    public final static String MOIRAI_USER_UNBINDUSER = "6";
    public final static String MOIRAI_USER_ORG = "8";

    //双因子模式
    public final static String MOIRAI_DUCL_FACTOR_DEFAULT = "0";
    public final static String MOIRAI_DUCL_FACTOR_PHONE = "1";
    public final static String MOIRAI_DUCL_FACTOR_EMAIL = "2";

    public static final String BWTOKEN = "BWTOKEN";

    public static final String REDIS_ACCESSTOKEN = "authSSO:accessToken:";

    public static final String JUDGE_PERMISSION = "judge:permission:";

    public static final String REDIS_USER = "authSSO:user:";

    public static final String DEFAULT_ZERO = "0";
    public static final String DEFAULT_ONE = "1";
    public static final String DEFAULT_TWO = "2";
    public static final String DEFAULT_THREE = "3";
    public static final String USER_TYPE_B = "B";
    public static final String USER_TYPE_C = "C";


    public final static String MOIRAI_VERSION_V2 = "v2";

    // 密码策略
    public final static String PASSWORD_POLICY = "passwordPolicy";
    // 登陆失败`{lockThreshold}`次，锁定用户。 0-不锁定，10-十次（默认）
    public final static String LOCK_THRESHOLD = "lockThreshold";
    // 登陆失败`{checkThreshold}`次，验证验证码。 0-验证码必填，3-三次（默认）
    public final static String CHECK_THRESHOLD = "checkThreshold";
    // 首次登陆`{firstLoginCheck}` 修改密码。 true-需要（默认），false-不需要
    public final static String FIRST_LOGIN_CHECK = "firstLoginCheck";
    // 密码有效期超过`{pwdValidity}`天，重新设置密码。 0-不限制（默认），验证用户名、旧密码、新密码、图片验证码正确后登陆成功
    public final static String PWD_VALIDITY = "pwdValidity";
    // 密码中不能包含账号`{pwdNoAccountCheck}`。true-校验， false-不校验（默认）
    public final static String PWD_NO_ACCOUNT_CHECK = "pwdNoAccountCheck";
    // 包含以下4种字符类型的任意{pwdTypeSize}种组合
    public final static String PWD_TYPE_SIZE = "pwdTypeSize";
    // 密码长度{pwdLength}，最小6位，最大30位
    public final static String PWD_LENGTH = "pwdLength";
    // 最近`{historySize}`次历史密码重复校验限制。0-不限制（默认），5-最近5次密码不能相同（不包括当前旧密码）
    public final static String HISTORY_SIZE = "historySize";


    //扩展字段
    public final static String MOIRAI_USER_PAGE_UNIQUE_NAME = "yhzx_yhgl";

    public final static String MOIRAI_USER_IMPORT_TAG = "yhglim";
    public final static String MOIRAI_USER_EXPORT_TAG = "yhglem";

    public final static String MOIRAI_ORG_PAGE_UNIQUE_NAME = "yhzx_jggl";

    public final static String MOIRAI_ORG_IMPORT_TAG = "jgglim";

    public final static String MOIRAI_ORG_EXPORT_TAG = "jgglem";

    //导入数据的Excel模板类型
    public final static String MOIRAI_EXCEL_TYPE_IMPORT = "2";

    //导出数据的Excel模板类型
    public final static String MOIRAI_EXCEL_TYPE_EXPORT = "0";

    public final static String PRIVATE_KEY = "MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAwzGb/7yjWiF3dkfMy7rGTrLS9sRMZaEe7qOKOMYq8Gs8g8aVqwUViwHV6mWJiKLjyCR1dIpTRmV7QdMRjmAxVQIDAQABAkBK6kJmP78uO4P/GIWId7HeyJFhdmuf4dveWVQWgDBAvyrJM+5QuFzo1Jl+MvbsbUrgODm53F6rdh5eYl82eWfBAiEA/HMrHhS8kTlhrsLVVdX6xQqFpmabcvN6sAh6K8GWgyUCIQDF8FD4n6Zo+CiH77vfjgPHCL2y3xgkOJwkZ/vy3qS2cQIgT1Mc/FHFwthGAnUENfmL17hrsJLTS7aIGaJASVmGIuUCIAMrEpC4NXiuqC9s1ewBIOxZpnerfH5v7YfjpjO//pDxAiEAq3mYoG3ieb8bjD273ipuhkprpz49sz/9p/XnylFS61k=";

    // 默认初始密码
    public static String INIT_PWD = "123456";

    @Autowired(required = false)
    public void setUploadPath(@Value("${user.initPwd:123456}")String INIT_PWD) {
        Constants.INIT_PWD = INIT_PWD;
    }

    //销项标准版
    public final static Long OUTPUT_STANDARD_PRODUCT = 10008L;

    //销项免费版
    public final static Long OUTPUT_FREE_PRODUCT = 10011L;

    public final static String ERP_BIND_METHODS = "baiwang.output.transaction.erpbatchimport,baiwang.invoice.flowSingleDelete,baiwang.invoice.FlowSingleQuery,baiwang.invoice.query,baiwang.isp.invoice.flowsheet.tree.query";
}
