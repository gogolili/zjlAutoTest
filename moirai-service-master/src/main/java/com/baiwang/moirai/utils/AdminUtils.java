package com.baiwang.moirai.utils;

import com.baiwang.moirai.model.role.MoiraiResource;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class AdminUtils {

    public static String getUuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static String getUuidPasswd(String passwd, String uuid) {
        if (StringUtils.isNotBlank(uuid)) {
            return Encrypt.md5AndSha(passwd + uuid);
        } else {
            return Encrypt.md5AndSha(passwd);
        }
    }

    /**
     * @param listMoiraiResource
     * @param id                 生成资源树，id传0，必须重根节点开始
     * @return
     * @clc
     */
    public static List<MoiraiResource> getResourceTree(List<MoiraiResource> listMoiraiResource, Long id) {
        if (StrUtils.isEmptyList(listMoiraiResource)) {
            return null;
        }
        List<MoiraiResource> list = new ArrayList<>();
        List<MoiraiResource> listContinue = new ArrayList<>(listMoiraiResource);

        for (MoiraiResource mr : listMoiraiResource) {
            if (mr.getPid().equals(id)) {//从顶级菜单开始pid=0

                listContinue.remove(mr);
                mr.setChildren(getResourceTree(listContinue, mr.getResourceId()));
                list.add(mr);
            }
        }

        if (list.size() == 0) {
            return null;
        }

        return list;
    }

    /**
     * 生成文件名
     *
     * @param fileExtName
     * @return String 返回类型
     * @Title: makeFileName
     */
    public static String makeFileName(String fileExtName) {
        // 为防止文件覆盖的现象发生，要为上传文件产生一个唯一的文件名
        String fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        return fileName + "." + fileExtName;
    }

    /**
     * 生成文件保存路径
     *
     * @param filename
     * @param savePath
     * @return String 返回类型
     * @Title: makePath
     */
    public static String makePath(String filename, String savePath) {

        String dir1 = new SimpleDateFormat("yyyyMMdd").format(new Date());//去除路径中的符号，防止不同系统下转义等处理错误
        String dir2 = new SimpleDateFormat("HHmmss").format(new Date());//去除路径中的符号，防止不同系统下转义等处理错误

        // 构造新的保存目录
        String dir = savePath + "/" + dir1 + "/" + dir2;

        // File既可以代表文件也可以代表目录
        File file = new File(dir);
        // 如果目录不存在
        if (!file.exists()) {
            // 创建目录
            file.mkdirs();
        }
        return dir;
    }

    /**
     * 生成短信验证码
     * @return
     */
    public static String getSmsValidateCode() {
        String[] beforeShuffle = new String[] {
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "0" };
        List<String> list = Arrays.asList(beforeShuffle);
        Collections.shuffle(list); // 打乱集合顺序
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
        }

        String afterShuffle = sb.toString();
        String result = afterShuffle.substring(0, 6);
        return result;
    }
}
