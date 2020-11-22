package com.baiwang.moirai.serviceimpl;

import com.alibaba.fastjson.JSONObject;
import com.baiwang.cloud.common.enumutil.ErrorType;
import com.baiwang.cloud.common.model.BWJsonResult;
import com.baiwang.cloud.common.model.ErrorMessage;
import com.baiwang.moirai.common.Constants;
import com.baiwang.moirai.common.WebContext;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.exception.MoiraiException;
import com.baiwang.moirai.feignclient.AppServiceClient;
import com.baiwang.moirai.feignclient.BopUserInfo;
import com.baiwang.moirai.feignclient.CostingServiceClient;
import com.baiwang.moirai.feignclient.SeqnumFeignClient;
import com.baiwang.moirai.feignclient.WorkorderServiceClient;
import com.baiwang.moirai.mapper.MoiraiUserAuthzMapper;
import com.baiwang.moirai.mapper.MoiraiUserMapper;
import com.baiwang.moirai.mapper.SysDictMapper;
import com.baiwang.moirai.mapper.SysProvCityDistMapper;
import com.baiwang.moirai.model.org.MoiraiOrg;
import com.baiwang.moirai.model.org.MoiraiOrgProduct;
import com.baiwang.moirai.model.role.MoiraiUserAuthz;
import com.baiwang.moirai.model.sys.SysDict;
import com.baiwang.moirai.model.sys.SysFileResponse;
import com.baiwang.moirai.model.sys.SysProvCityDist;
import com.baiwang.moirai.model.sys.SysProvCityDistTree;
import com.baiwang.moirai.model.user.MoiraiUser;
import com.baiwang.moirai.service.MoiraiSysService;
import com.baiwang.moirai.service.PasswordService;
import com.baiwang.moirai.utils.AdminUtils;
import com.baiwang.moirai.utils.HttpClientUtil;
import com.baiwang.moirai.utils.HttpInfoUtils;
import com.baiwang.moirai.utils.StrUtils;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.beanutils.LazyDynaBean;
import org.apache.poi.util.IOUtils;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MoiraiSysServiceImpl implements MoiraiSysService {
    private Logger logger = LoggerFactory.getLogger(MoiraiSysServiceImpl.class);

    @Autowired
    private SysProvCityDistMapper sysProvCityDistMapper;
    @Autowired
    private SysDictMapper sysDictMapper;

    @Autowired
    private SeqnumFeignClient seqnumFeignClient;

    @Autowired
    private WorkorderServiceClient workorderServiceClient;

    @Autowired
    private MoiraiUserMapper moiraiUserMapper;

    @Resource(name = "loadNoBalanced")
    @Autowired
    private RestTemplate restTemplate;

    private static final Integer MAX_SIZE = 1024 * 1024 * 10;

    @Autowired
    private MoiraiUserAuthzMapper moiraiUserAuthzMapper;

    @Autowired
    @Qualifier("redisTrafficTemplate")
    private RedisTemplate redisTemplate;

    @Value("${user.openCloud}")
    private String cloudUrl;

    @Value("${use.method}")
    private boolean useFlag;

    @Value("${use.tenant.taxcode.method}")
    private boolean userTenantMethod;

    @Value("${uploadUrl}")
    private String uploadUrl;

    @Value("${downloadUrl}")
    private String downloadUrl;

    @Value("${moirai.gateway.ip}")
    private String currentIp;

    @Autowired(required = false)
    private PasswordService passwordService;

    @Autowired(required = false)
    private AppServiceClient appServiceClient;

    @Autowired(required = false)
    private CostingServiceClient costingServiceClient;

    @Override
    public List<SysProvCityDist> getSysProvCityDist(SysProvCityDist sys) {
        List<SysProvCityDist> sysProvCityDistList = null;
        try {
            if ("1".equals(sys.getFunction())) {
                sysProvCityDistList = sysProvCityDistMapper.selectByBeanVague(sys);
            } else {
                sysProvCityDistList = sysProvCityDistMapper.selectByBean(sys);
            }
        } catch (Exception e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_GET_DICTINFO_ERROR;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);
            throw new MoiraiException(MoiraiErrorEnum.Moirai_DB_ERROR);
        }
        return sysProvCityDistList;
    }

    @Override
    public List<SysProvCityDistTree> getSysProvCityDistTree() {
        List<SysProvCityDistTree> sysProvCityDistList = sysProvCityDistMapper.selectAll();
        List<SysProvCityDistTree> type0 = sysProvCityDistList.stream().filter(prov -> prov.getType().equals("0")).collect(Collectors.toList());
        for (SysProvCityDistTree prov : type0) {
            prov.setChildren(getProvCityDistTree(sysProvCityDistList, prov.getId()));
        }
        return type0;
    }

    /**
     * 根据树根结点id，构造整棵树
     *
     * @param provCityDists
     * @param id
     * @return
     */
    private List<SysProvCityDistTree> getProvCityDistTree(List<SysProvCityDistTree> provCityDists, Integer id) {
        if (StrUtils.isEmptyList(provCityDists)) {
            return null;
        }
        List<SysProvCityDistTree> list = new ArrayList<>();
        List<SysProvCityDistTree> listContinue = new ArrayList<>(provCityDists);

        for (SysProvCityDistTree prov : provCityDists) {
            if (prov.getCityId().equals(id)) {
                listContinue.remove(prov);
                prov.setChildren(getProvCityDistTree(listContinue, prov.getId()));
                list.add(prov);
            }
        }

        if (list.size() == 0) {
            return null;
        }

        return list;
    }

    @Override
    public List<SysDict> getSysDict(SysDict sysDict) {
        logger.info("字典信息查询 :sysDict={}", sysDict);
        return sysDictMapper.selectWholeDictInfo(sysDict);
    }

    @Override
    public List<SysDict> getSysDictBatch(List<String> dictTypeList) {
        logger.info("字典信息查询 :dictTypeList={}", dictTypeList);
        List<SysDict> sysDictBatch = sysDictMapper.getSysDictBatch(dictTypeList);
        return sysDictBatch;
    }

    /**
     * <B>方法名称：</B>现在只判断B端用户<BR>
     * <B>概要说明：</B>判断当前用户是否有操作传入用户的权限<BR>
     *
     * @return
     * @since 2020/2/13
     */
    @Override
    public boolean getUserOfLanders(MoiraiUser moiraiUser) {
        MoiraiUser user = this.gainCacheUser();
        if (user != null) {
            Long pUserId = moiraiUser.getUserId();
            //获取用户信息
            Long userId = user.getUserId();
            if (userId.equals(pUserId)) {
                return true;
            }
            Long orgId = moiraiUser.getOrgId();
            List<MoiraiUserAuthz> moiraiUserAuthzs = getAuthzs(userId, orgId, user.getTenantId());
            if (moiraiUserAuthzs.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<MoiraiUserAuthz> getAuthzs(Long userId, Long orgId, Long tenantId) {
        MoiraiUserAuthz authz = new MoiraiUserAuthz();
        authz.setUserId(userId);
        authz.setAuthOrg(orgId);
        authz.setTenantId(tenantId);
        return moiraiUserAuthzMapper.selectByBean(authz);
    }

    /**
     * <B>方法名称：</B><BR>
     * <B>概要说明：</B>根据请求获取缓存中的用户信息<BR>
     *
     * @return
     * @since 2020/2/19
     */
    @Override
    public MoiraiUser gainCacheUser() {
        HttpServletRequest request = WebContext.getRequest();
        String bwToken = HttpInfoUtils.getToken(Constants.BWTOKEN, request);
        if (bwToken != null) {
            Object token = redisTemplate.opsForValue().get(Constants.REDIS_ACCESSTOKEN + bwToken);
            if (token != null) {
                HashMap hashMap = JSONObject.parseObject(token.toString(), HashMap.class);
                Object oUserId = hashMap.get("userId");
                return moiraiUserMapper.selectByPrimaryKey(Long.valueOf(oUserId.toString()));
            }
        }
        return null;
    }

    /**
     * <B>方法名称：</B><BR>
     * <B>概要说明：</B>根据请求获取缓存中的用户id<BR>
     *
     * @return
     * @since 2020/2/19
     */
    @Override
    public Long gainCacheUserId() {
        HttpServletRequest request = WebContext.getRequest();
        String bwToken = HttpInfoUtils.getToken(Constants.BWTOKEN, request);
        if (bwToken != null) {
            Object token = redisTemplate.opsForValue().get(Constants.REDIS_ACCESSTOKEN + bwToken);
            if (token != null) {
                HashMap hashMap = JSONObject.parseObject(token.toString(), HashMap.class);
                Object oUserId = hashMap.get("userId");
                if (oUserId != null) {
                    return Long.valueOf(oUserId.toString());
                }
            }
        }
        return null;
    }

//    @Override
//    public Map<String, Object> getProvCityDist(String taxNo) throws Exception {
//        Map<String, Object> rtn = new HashMap<>();
//        Integer id;
//        try {
//            if ("9".equals(taxNo.substring(0, 1))) {
//                // 取3,8位
//                id = Integer.parseInt(taxNo.substring(2, 8));
//            } else {
//                // 取1,6位
//                id = Integer.parseInt(taxNo.substring(0, 6));
//            }
//        } catch (Exception e) {
//            rtn.put("code", "-1");
//            rtn.put("msg", "税号有问题1");
//            return rtn;
//        }
//        SysProvCityDist sysProvCityDist = new SysProvCityDist();
//        sysProvCityDist.setId(id);
//        List<SysProvCityDist> sysProvCityDistList = sysProvCityDistMapper.selectByBeanVague(sysProvCityDist);
//        if (StrUtils.isEmptyList(sysProvCityDistList)) {
//            rtn.put("code", "-1");
//            rtn.put("msg", "税号有问题2");
//            return rtn;
//        }
//        SysProvCityDist dist = sysProvCityDistList.get(0);
//        if ("2".equals(dist.getType())) {
//            rtn.put("distName", dist.getDistrict());
//            rtn.put("distId", dist.getId());
//            sysProvCityDist = new SysProvCityDist();
//            sysProvCityDist.setId(dist.getCityId());
//            sysProvCityDistList = sysProvCityDistMapper.selectByBeanVague(sysProvCityDist);
//            if (StrUtils.isEmptyList(sysProvCityDistList)) {
//                rtn.put("code", "-1");
//                rtn.put("msg", "税号有问题3");
//                return rtn;
//            }
//            SysProvCityDist city = sysProvCityDistList.get(0);
//            if ("1".equals(city.getType())) {
//                rtn.put("cityName", dist.getDistrict());
//                rtn.put("cityId", dist.getId());
//                sysProvCityDist = new SysProvCityDist();
//                sysProvCityDist.setId(city.getProvinceId());
//                sysProvCityDistList = sysProvCityDistMapper.selectByBeanVague(sysProvCityDist);
//                if (StrUtils.isEmptyList(sysProvCityDistList)) {
//                    rtn.put("code", "-1");
//                    rtn.put("msg", "税号有问题4");
//                    return rtn;
//                }
//                SysProvCityDist prov = sysProvCityDistList.get(0);
//                if ("0".equals(prov.getType())) {
//                    rtn.put("provName", dist.getProvince());
//                    rtn.put("provId", dist.getId());
//                    rtn.put("code", "0");
//                    return rtn;
//                }
//            }
//
//
//        }
//        rtn.put("code", "-1");
//        rtn.put("msg", "税号有问题5");
//        return rtn;
//    }

//    @Override
//    public MoiraiOrg getProvCityDistToOrg(MoiraiOrg org) throws Exception {
//        if (org == null) {
//            return null;
//        }
//        if (org.getRegProv() == null && org.getRegArea() == null || org.getRegCity() == null) {
//
//            Map<String, Object> rtn = getProvCityDist(org.getTaxCode());
//
//            if ("-1".equals(rtn.get("code"))) {
//                return org;
//            }
//            org.setRegProv((long) ((Integer) rtn.get("provId")));
//            org.setRegCity((long) ((Integer) rtn.get("cityId")));
//            org.setRegArea((long) ((Integer) rtn.get("distId")));
//
//
//        }
//
//        return org;
//    }

    /**
     * @param param
     * @param flag 1：百望云存储tenantId，2：百望云存储税号，3：机构相关防重，4：用户相关防重
     * @return
     */
    @Override
    public JSONObject commonMethod(Map<String, String> param, int flag) {
        switch (flag) {
            case 3:
                if (useFlag) {
                    //百望云老版用户中心字段防重复
                    logger.info("============== " + "需要判重字段:" + param + "，请求地址：" + cloudUrl + Constants.USER_OPENCLOUD + " ===============");
                    Map<String, Object> paramer = new HashMap<>();
                    paramer.put("type", "org");
                    paramer.put("content", param);
                    JSONObject jsonObject = restTemplate.postForObject(cloudUrl + Constants.USER_OPENCLOUD, paramer, JSONObject.class);
                    return jsonObject;
                }
                break;
            case 4:
                if (useFlag) {
                    //百望云老版用户中心字段防重复
                    logger.info("============== " + "需要判重字段:" + param + "，请求地址：" + cloudUrl + Constants.USER_OPENCLOUD + " ===============");
                    Map<String, Object> paramer = new HashMap<>();
                    paramer.put("type", "user");
                    paramer.put("content", param);
                    JSONObject jsonObject = restTemplate.postForObject(cloudUrl + Constants.USER_OPENCLOUD, paramer, JSONObject.class);
                    return jsonObject;
                }
                break;
            default:
                break;
        }
        return null;
    }

    /**
     * <B>方法名称：</B>上传至oss方法<BR>
     * <B>概要说明：</B><BR>
     *
     * @return
     * @since 2019年3月30日
     */
    @Override
    public BWJsonResult uploadFile(MultipartFile item, String tempPath) {
        // 消息提示
        try {
            File tmpFile = new File(tempPath);
            DiskFileItemFactory factory = new DiskFileItemFactory();
            // 设置缓冲区的大小为100KB，如果不指定，那么缓冲区的大小默认是10KB
            factory.setSizeThreshold(1024 * 100);
            factory.setRepository(tmpFile);
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setHeaderEncoding("UTF-8");

            upload.setFileSizeMax(MAX_SIZE);
            upload.setSizeMax(MAX_SIZE);

            if (item.getSize() > MAX_SIZE) {
                return new BWJsonResult(new MoiraiException(MoiraiErrorEnum.MOIRAI_SYS_FILE_TOL_ERROR));
            }
            // 得到上传的文件名称，
            String filename = item.getOriginalFilename();
            if (filename == null || filename.trim().equals("")) {
                return new BWJsonResult(new MoiraiException(MoiraiErrorEnum.MOIRAI_PARAMS_MISS_ERROR));
            }
            filename = filename.substring(filename.lastIndexOf("/") + 1);
            String fileExtName = filename.substring(filename.lastIndexOf(".") + 1);

            InputStream in = item.getInputStream();
            String saveFilename = AdminUtils.makeFileName(fileExtName);
            String realSavePath = AdminUtils.makePath(saveFilename, "specialUse");
            String filurl = realSavePath + "/" + saveFilename;
            int result = HttpClientUtil.sendPostFiles(uploadUrl + filurl, in);
            in.close();
            logger.info("文件下载路径是：" + downloadUrl + filurl);
            if (result == 1) {
                return new BWJsonResult(new SysFileResponse(downloadUrl + filurl));
            } else {
                return new BWJsonResult(new MoiraiException(MoiraiErrorEnum.MOIRAI_SYS_FILE_IN_ERROR));
            }
        } catch (Exception e) {
            logger.error("文件上传失败", e);
            return new BWJsonResult(new MoiraiException(MoiraiErrorEnum.MOIRAI_TENANT_ERROR));
        }
    }

    /**
     * <B>方法名称：</B><BR>
     * <B>概要说明：</B>批量获取各个表的ID<BR>
     *
     * @return
     * @since 2019年3月6日
     */
    @Override
    public List<Long> getNums(List list, String moirai) {
        int count = list.size() / 1000;
        List<Long> ids = new ArrayList<>();
        while (count >= 0) {
            if (count > 0) {
                List<Long> nums = seqnumFeignClient.getNums(moirai, 1000);
                ids.addAll(nums);
            } else {
                ids.addAll(seqnumFeignClient.getNums(moirai, list.size() % 1000));
            }
            count--;
        }
        return ids;
    }

    /**
     * <B>方法名称：</B>上传错误文件到oss<BR>
     * <B>概要说明：</B><BR>
     *
     * @return
     * @since 2019年3月30日
     */
    public BWJsonResult<SysFileResponse> uploadErrorFile(StringBuffer str, String fileName) {
        outIO(str.toString(), fileName);
        String path = Constants.ERRORFILE_LOCATION + fileName;
        File file1 = new File(path);
        BWJsonResult bwJsonResult = null;
        try {
            InputStream in = new FileInputStream(file1);
            bwJsonResult = this.uploadErrorFile(in, fileName);
            in.close();
        } catch (IOException e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_UPLOAD_ERROR_FILE_ERROR;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);
            throw new MoiraiException(errorEnum);
        }
        return bwJsonResult;
    }

    /**
     * <B>方法名称：</B>上传错误文件到oss<BR>
     * <B>概要说明：</B><BR>
     *
     * @return
     * @since
     */
    public BWJsonResult<SysFileResponse> uploadErrorFile(InputStream inputStream, String fileName) {
        String path = Constants.ERRORFILE_LOCATION + fileName;
        String pathUrl = path.substring(path.lastIndexOf("/") + 1);
        String fileExtName = pathUrl.substring(pathUrl.lastIndexOf(".") + 1);
        String saveFilename = AdminUtils.makeFileName(fileExtName);
        String realSavePath = AdminUtils.makePath(saveFilename, "specialUse");
        String filurl = realSavePath + "/" + saveFilename;
        int result = HttpClientUtil.sendPostFiles(uploadUrl + filurl, inputStream);

        logger.info("文件保存路径是：" + uploadUrl + filurl);
        logger.info("文件下载路径是：" + downloadUrl + filurl);
        File file = new File(path);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            file.delete();
        }
        if (result == 1) {
            BWJsonResult<SysFileResponse> bwJsonResult = new BWJsonResult(new SysFileResponse(downloadUrl + filurl));
            bwJsonResult.setMessage("导入失败");
            bwJsonResult.setSuccess(false);
            return bwJsonResult;
        } else {
            return new BWJsonResult(new MoiraiException(MoiraiErrorEnum.MOIRAI_SYS_FILE_IN_ERROR));
        }
    }

    /**
     * <B>方法名称：</B>生成本地临时文件存储错误信息<BR>
     * <B>概要说明：</B><BR>
     *
     * @return
     * @since 2019年3月6日
     */
    private void outIO(String str, String fileName) {
        String path = Constants.ERRORFILE_LOCATION;
        FileWriter writer = null;
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        try {
            writer = new FileWriter(path + fileName, true);
            writer.write(str);
        } catch (IOException e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_IO_EXCEPTION;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);

        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                String requestURI = WebContext.getRequest().getRequestURI();
                MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_IO_EXCEPTION;
                logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);

            }
        }
    }

    /**
     * <B>方法名称：</B>去1.0校验机构或用户信息<BR>
     * <B>概要说明：</B>开多个线程，每个线程处理20条数据<BR>
     *
     * @return
     * @since 2019年3月6日
     */
    public void excecutorOldDB(List<ArrayList<String>> excelList, StringBuffer str, int tag) {
        class OrgThread implements Runnable {
            private List<ArrayList<String>> list;
            private CountDownLatch end;

            //初始化数据
            public OrgThread(List<ArrayList<String>> list, CountDownLatch end) {
                this.list = list;
                this.end = end;
            }

            @Override
            public void run() {
                for (int i = 1; i < list.size(); i++) {
                    //查询百望云老版数据库
                    Map<String, String> map = new HashMap<>();
                    if (tag == 3) {
                        map.put("taxCode", list.get(i).get(3));
                    } else if (tag == 4) {
                        map.put("username", list.get(i).get(0));
                    }
                    JSONObject jsonObject = commonMethod(map, tag);
                    if (null != jsonObject && !"0".equals(jsonObject.get("code"))) {
                        String message = (String) jsonObject.get("message");
                        if (message.contains("组织名称已存在")) {
                            str.append(list.get(i).get(1) + "，" + message + ";\r\n");
                        } else if (message.contains("组织税号已存在")) {
                            str.append(list.get(i).get(3) + "，" + message + ";\r\n");
                        } else if (message.contains("用户账号已存在")) {
                            str.append(list.get(i).get(0) + "，" + message + ";\r\n");
                        } else {
                        }
                    }
                }
                end.countDown();
            }
        }

        int count = 200;
        int listSize = excelList.size();
        int runSize = (listSize % count) == 0 ? listSize / count : listSize / count + 1;//开启线程数
        ExecutorService executor = Executors.newFixedThreadPool(runSize);
        CountDownLatch end = new CountDownLatch(runSize);
        List<ArrayList<String>> newlist = null;//存放每个线程的执行数据
        for (int i = 0; i < runSize; i++) {
            //计算每个线程执行的数据
            if ((i + 1) == runSize) {
                int startIndex = (i * count);
                int endIndex = listSize;
                newlist = excelList.subList(startIndex, endIndex);
            } else {
                int startIndex = (i * count);
                int endIndex = (i + 1) * count;
                newlist = excelList.subList(startIndex, endIndex);
            }
            //线程类
            OrgThread mythead = new OrgThread(newlist, end);
            //这里执行线程的方式是调用线程池里的executor.execute(mythead)方法。
            executor.execute(mythead);
        }
        try {
            end.await();
        } catch (InterruptedException e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_TENANT_ERROR;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);
            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_TENANT_ERROR);
        }
        //执行完关闭线程池
        executor.shutdown();
    }

    /**
     * <B>方法名称：</B>上传错误信息到redis<BR>
     * <B>概要说明：</B>大客户本地部署使用<BR>
     *
     * @return
     * @since 2019年4月23日
     */
    public BWJsonResult uploadString(String str, String path, Long orgId) {
        redisTemplate.opsForValue().set(path + orgId, str, 1L, TimeUnit.HOURS);
        if (Constants.MOIRAI_ORG_ERRORFILE.equals(path)) {
            BWJsonResult bwJsonResult = new BWJsonResult(new SysFileResponse(currentIp + "/moirai/moiraiGet/downloadString?path=MOIRAI_ORG&orgId=" + orgId));
            bwJsonResult.setMessage("导入失败");
            bwJsonResult.setSuccess(false);
            return bwJsonResult;
        } else if (Constants.MOIRAI_USER_ERRORFILE.equals(path)) {
            BWJsonResult bwJsonResult = new BWJsonResult(new SysFileResponse(currentIp + "/moirai/moiraiGet/downloadString?path=MOIRAI_USER&orgId=" + orgId));
            bwJsonResult.setMessage("导入失败");
            bwJsonResult.setSuccess(false);
            return bwJsonResult;
        } else {
            return new BWJsonResult(new MoiraiException(MoiraiErrorEnum.MOIRAI_SYS_FILE_IN_ERROR));
        }
    }

    /**
     * <B>方法名称：</B>上传错误信息到redis<BR>
     * <B>概要说明：</B>大客户本地部署使用<BR>
     *
     * @return
     * @since 2019年4月23日
     */
    @Override
    public void downloadString(String path, Long orgId, HttpServletResponse response) {
        String filePath = null;
        if (Constants.MOIRAI_USER.equals(path)) {
            filePath = Constants.MOIRAI_USER_ERRORFILE;
        } else if (Constants.MOIRAI_ORG.equals(path)) {
            filePath = Constants.MOIRAI_ORG_ERRORFILE;
        } else {
        }
        String errorString = (String) redisTemplate.opsForValue().get(filePath + orgId);
        response.setContentType("text/plain");
        response.setContentType("multipart/form-data");
        try {
            response.addHeader("Content-Disposition", "attachment; filename=\"" +
                new String("excel错误提示信息.txt".getBytes("UTF-8"), "ISO8859-1") + "\"");
        } catch (UnsupportedEncodingException e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_DE_ENCODE_ERROR;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);
        }
        ByteArrayInputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = new ByteArrayInputStream(errorString.getBytes());
            outputStream = new BufferedOutputStream(response.getOutputStream());
            byte[] bytes = new byte[1024];
            int n;
            while ((n = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, n);
            }
        } catch (IOException e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_IO_EXCEPTION;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);

            throw new MoiraiException(MoiraiErrorEnum.MOIRAI_TENANT_ERROR);
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                String requestURI = WebContext.getRequest().getRequestURI();
                MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_IO_EXCEPTION;
                logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);
            }
        }
    }

    /**
     * <B>方法名称：</B>下载Excel模版<BR>
     * <B>概要说明：</B>用户和机构模版<BR>
     *
     * @return
     * @since 2019年4月28日
     */
    @Override
    public void downloadTemplate(HttpServletRequest request, HttpServletResponse response, String fileName,
        String path) {
        InputStream inputStream = null;
        ServletOutputStream servletOutputStream = null;
        try {

            DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
            org.springframework.core.io.Resource resource = resourceLoader.getResource("classpath:" + path);

            // 判断使用的浏览器
            String agent = request.getHeader("USER-AGENT");
            if (null != agent && -1 != agent.indexOf("MSIE")) {
                response.addHeader("Content-Disposition",
                    "attachment; filename=\"" + java.net.URLEncoder.encode(fileName, "UTF-8").replace("+", " ") + "\"");
            } else if (null != agent && -1 != agent.indexOf("Firefox")) {
                response.addHeader("Content-Disposition",
                    "attachment; filename=\"" + new String(fileName.getBytes(), "iso8859-1") + "\"");
            } else {
                response.setHeader("Content-Disposition", "attachment;filename="
                    + new String(java.net.URLEncoder.encode(fileName, "utf-8").getBytes(), "iso8859-1"));
            }
            response.setContentType("application/vnd.ms-excel");

            inputStream = resource.getInputStream();
            servletOutputStream = response.getOutputStream();
            IOUtils.copy(inputStream, servletOutputStream);
            response.flushBuffer();
        } catch (Exception e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_IO_EXCEPTION;
            logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);

        } finally {
            try {
                if (servletOutputStream != null) {
                    servletOutputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                String requestURI = WebContext.getRequest().getRequestURI();
                MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_IO_EXCEPTION;
                logger.error(new ErrorMessage(requestURI, errorEnum.getCode(), errorEnum.getMsg(), ErrorType.CustomerError).toString(), e);

            }
        }
    }

    /**
     * <B>方法名称：</B>获取字典code<BR>
     * <B>概要说明：</B>用于Excel导入校验<BR>
     *
     * @return
     * @since 2019年4月28日
     */
    public List<String> getDictCode(String dictType) {
        SysDict sysDict = new SysDict();
        sysDict.setDictType(dictType);
        List<SysDict> dict = this.getSysDict(sysDict);
        List<String> dictCode = new ArrayList<>();
        dict.forEach(item -> dictCode.add(item.getDictCode()));
        return dictCode;
    }

    /**
     * <B>方法名称：</B><BR>
     * <B>概要说明：</B>单独开一个线程去同步租户ID<BR>
     *
     * @return
     * @since 2019年3月18日
     */
    @Override
    public void excecutorImport(List<Long> tenantIds, List<String> taxCodeList, List<MoiraiOrg> moiraiOrgList) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    if (userTenantMethod && taxCodeList != null) {
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
                        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
                        HttpEntity<List> formEntity = new HttpEntity<>(tenantIds, headers);
                        String postRes = null;
                        if (tenantIds != null) {
                            postRes = restTemplate.postForObject(cloudUrl + Constants.TENANTID_OPENCLOUD, formEntity, String.class);
                            logger.info("同步租户ID返回:{}", postRes);
                        }
                        if (!taxCodeList.isEmpty()) {
                            formEntity = new HttpEntity<>(taxCodeList, headers);
                            postRes = restTemplate.postForObject(cloudUrl + Constants.TAXCODE_OPENCLOUD, formEntity, String.class);
                            logger.info("同步税号返回:{}", postRes);
                        }
                    }
                    if (moiraiOrgList != null) {
                        BWJsonResult bwJsonResult = workorderServiceClient.batchWorkorder(moiraiOrgList);
                        logger.info("同步工单信息返回:{}", bwJsonResult);
                    }
                } catch (Exception e) {
                    logger.error("同步信息失败", e);
                }
            }
        });
        //执行完关闭线程池
        executor.shutdown();
    }

    @Override
    public void removeTaxCode(String taxCode) {
        String result = restTemplate.getForObject(cloudUrl + Constants.REMOVE_TAXCODE_OPENCLOUD + taxCode, String.class);
        logger.info("调用接口移除hub税号返回:{}", result);
    }

    /**
     * <B>方法名称：</B><BR>
     * <B>概要说明：</B>单独开一个线程去发送邮件<BR>
     *
     * @return
     * @since 2019年7月30日
     */
    public void excecutorSendEmail(List<LazyDynaBean> lazyDynaBeans, String titleFrom) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(new Runnable() {
            @Override
            public void run() {
                String titleInfo = "";
                switch (titleFrom) {
                    case Constants.DEFAULT_ZERO:
                        titleInfo = "租户";
                        Map<String, List<LazyDynaBean>> emailMap = tenantSendEmail(lazyDynaBeans);
                        passwordService.sendTenantMail(Constants.MOIRAI_VERSION_V2, "百望云", "ZC_VERIFY",
                            "百望云平台" + titleInfo + "注册成功", emailMap);
                        break;
                    case Constants.DEFAULT_ONE:
                        titleInfo = "机构";
                        passwordService.sendMail(Constants.MOIRAI_VERSION_V2, "百望云", "ZC_VERIFY",
                            "百望云平台" + titleInfo + "注册成功", lazyDynaBeans);
                        break;
                    case Constants.DEFAULT_TWO:
                        titleInfo = "账号";
                        passwordService.sendMail(Constants.MOIRAI_VERSION_V2, "百望云", "ZC_VERIFY",
                            "百望云平台" + titleInfo + "注册成功", lazyDynaBeans);
                        break;
                    case Constants.DEFAULT_THREE:
                        titleInfo = "租户";
                        addTenantSendEmail(lazyDynaBeans);
                        passwordService.sendMail(Constants.MOIRAI_VERSION_V2, "百望云", "ZC_VERIFY",
                            "百望云平台" + titleInfo + "注册成功", lazyDynaBeans);
                        break;
                    default:
                        break;
                }
            }
        });
        executor.shutdown();
    }

    private Map<String, List<LazyDynaBean>> tenantSendEmail(List<LazyDynaBean> lazyDynaBeans) {
        Map<String, List<LazyDynaBean>> emailInfo = new HashMap<>();
        for (LazyDynaBean lazyDynaBean : lazyDynaBeans) {
            Object org = lazyDynaBean.get("moiraiOrg");
            if (org != null) {
                MoiraiOrg moiraiOrg = (MoiraiOrg) org;
                List<MoiraiOrgProduct> products = moiraiOrg.getProducts();
                boolean opErpApi = false;
                Map<String, Object> bopInfo = null;
                for (MoiraiOrgProduct product : products) {
                    // 开放平台
                    if (product.getProductId().equals(8L)) {
                        bopInfo = createBopInfo(moiraiOrg);
                        if (bopInfo != null) {
                            lazyDynaBean.set("clientId", bopInfo.get("clientId"));
                            lazyDynaBean.set("clientSecret", bopInfo.get("clientSecret"));
                        }
                    }
                    if (product.getProductId().equals(10059L)) {
                        opErpApi = true;
                    }
                }
                // 统一入驻-ERP
                if (opErpApi) {
                    if (bopInfo == null) {
                        bopInfo = queryClientInfoByTenantId(moiraiOrg.getTenantId());
                    }
                    if (bopInfo != null) {
                        Map<String, Object> appkeyApi = new HashMap<>();
                        appkeyApi.put("appKey", bopInfo.get("clientId"));
                        appkeyApi.put("methods", Constants.ERP_BIND_METHODS);
                        appkeyApi.put("lasyModifyUser", "admin_" + moiraiOrg.getOrgId());
                        appkeyApi.put("requestId", bopInfo.get("clientId"));
                        appKeyBindApi(appkeyApi);
                    }
                }
            }
            String userEmail = ((List) lazyDynaBean.get("emails")).get(0).toString();
            List<LazyDynaBean> beans = emailInfo.get(userEmail);
            if (beans == null) {
                List<LazyDynaBean> emailLazyDyna = new ArrayList<>();
                emailLazyDyna.add(lazyDynaBean);
                emailInfo.put(userEmail, emailLazyDyna);
            } else {
                beans.add(lazyDynaBean);
            }
        }
        return emailInfo;
    }

    private void addTenantSendEmail(List<LazyDynaBean> lazyDynaBeans) {
        for (LazyDynaBean lazyDynaBean : lazyDynaBeans) {
            Object org = lazyDynaBean.get("moiraiOrg");
            if (org != null) {
                MoiraiOrg moiraiOrg = (MoiraiOrg) org;
                List<MoiraiOrgProduct> products = moiraiOrg.getProducts();
                boolean opErpApi = false;
                Map<String, Object> bopInfo = null;
                for (MoiraiOrgProduct product : products) {
                    // 开放平台
                    if (product.getProductId().equals(8L)) {
                        bopInfo = createBopInfo(moiraiOrg);
                        if (bopInfo != null) {
                            String contextStr = lazyDynaBean.get("context").toString();
                            contextStr += "<br>&nbsp;&nbsp;&nbsp;&nbsp;APP-key：" + bopInfo.get("clientId");
                            contextStr += "<br>&nbsp;&nbsp;&nbsp;&nbsp;APP-secret：" + bopInfo.get("clientSecret");
                            lazyDynaBean.set("context", contextStr);
                        }
                    }
                    if (product.getProductId().equals(10059L)) {
                        opErpApi = true;
                    }
                }
                // 统一入驻-ERP
                if (opErpApi) {
                    if (bopInfo == null) {
                        bopInfo = queryClientInfoByTenantId(moiraiOrg.getTenantId());
                    }
                    if (bopInfo != null) {
                        Map<String, Object> appkeyApi = new HashMap<>();
                        appkeyApi.put("appKey", bopInfo.get("clientId"));
                        appkeyApi.put("methods", Constants.ERP_BIND_METHODS);
                        appkeyApi.put("lasyModifyUser", "admin_" + moiraiOrg.getOrgId());
                        appkeyApi.put("requestId", bopInfo.get("clientId"));
                        appKeyBindApi(appkeyApi);
                    }
                }
            }
        }
    }

    /**
     * <B>方法名称：</B><BR>
     * <B>概要说明：</B>请求开放平台生成appkey<BR>
     *
     * @return
     * @since 2020年7月01日
     */
    private Map<String, Object> createBopInfo(MoiraiOrg moiraiOrg) {
        try {
            if (passwordService.calculatePasswordMark()) {
                BopUserInfo bopUserInfo = new BopUserInfo();
                bopUserInfo.setLoginId("admin_" + moiraiOrg.getOrgId());
                bopUserInfo.setUserName("机构管理员");
                bopUserInfo.setTenantId(moiraiOrg.getTenantId() + "");
                bopUserInfo.setPhone(moiraiOrg.getTelphone());
                bopUserInfo.setAddress(moiraiOrg.getBusinessAddress());
                bopUserInfo.setCompanyName(moiraiOrg.getOrgName());
                bopUserInfo.setCompanySh(moiraiOrg.getTaxCode());
                BWJsonResult bwJsonResult = appServiceClient.addDeveloperForRegister(bopUserInfo);
                if (bwJsonResult.isSuccess()) {
                    Map<String, Object> bopInfo = (Map<String, Object>) bwJsonResult.getData().get(0);
                    logger.info("调用app-service返回:{}", bopInfo);
                    Map<String, Object> map = new HashMap<>();
                    map.put("appkey", bopInfo.get("clientId"));
                    map.put("costingruleId", "10000002");
                    map.put("beginTime", LocalDate.now());
                    map.put("endTime", LocalDate.now().plusYears(1));
                    int i = costingServiceClient.addDeveloperCostingRule(map);
                    return bopInfo;
                }
            }
        } catch (Exception e) {
            logger.error("调用app-service或COSTING-SERVICE异常", e);
        }
        return null;
    }

    /**
     * <B>方法名称：</B><BR>
     * <B>概要说明：</B>请求开放平台生成appkey<BR>
     *
     * @return
     * @since 2020年7月07日
     */
    private void appKeyBindApi(Map<String, Object> appkeyApi) {
        try {
            BWJsonResult bwJsonResult = appServiceClient.appKeyBindApi(appkeyApi);
            logger.info("注册企业开通10059应用appKey：{},绑定开放平台api：{}", appkeyApi.get("appKey"), bwJsonResult.isSuccess());
        } catch (Exception e) {
            logger.error("调用app-service异常", e);
        }
    }

    /**
     * <B>方法名称：</B><BR>
     * <B>概要说明：</B>请求开放平台查询appkey<BR>
     *
     * @return
     * @since 2020年7月07日
     */
    private Map<String, Object> queryClientInfoByTenantId(Long tenantId) {
        BWJsonResult bwJsonResult = appServiceClient.queryClientInfoByTenantId(tenantId);
        if (bwJsonResult.isSuccess() && bwJsonResult.getData() != null) {
            return (Map<String, Object>) bwJsonResult.getData().get(0);
        }
        return null;
    }

    /*public void sendEmail(String toUser, String subject, String content) throws MessagingException {
        MailUtils  mailUtils = new MailUtils(from, mailSender);
        mailUtils.sendHtmlMail(toUser, subject, content);
    }*/
}
