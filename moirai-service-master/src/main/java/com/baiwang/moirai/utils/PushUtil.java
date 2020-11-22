package com.baiwang.moirai.utils;

import com.baiwang.cloud.common.enumutil.ErrorType;
import com.baiwang.cloud.common.model.ErrorMessage;
import com.baiwang.moirai.common.WebContext;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * 只是写的一个示例，filePath,和FileName根据需要进行调整。
 */
public class PushUtil {

    private static final Logger logger = LoggerFactory.getLogger(PushUtil.class);

    public static int access(String URLString) {
        try {
            StringBuffer response = new StringBuffer();

            HttpClient client = new HttpClient();
            PostMethod method = new PostMethod(URLString);

            // 设置Http Post数据，这里是上传文件
            File f = new File("/Users/baiwang/Downloads/importOrg.xlsx");
            FileInputStream fi = new FileInputStream(f);
            InputStreamRequestEntity fr = new InputStreamRequestEntity(fi);
            method.setRequestEntity((RequestEntity) fr);
            try {
                client.executeMethod(method); // 这一步就把文件上传了
                // 下面是读取网站的返回网页，例如上传成功之类的
                if (method.getStatusCode() == HttpStatus.SC_OK) {
                    // 读取为 InputStream，在网页内容数据量大时候推荐使用
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(method.getResponseBodyAsStream(), "UTF-8"));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                }
            } catch (IOException e) {
                String requestURI = WebContext.getRequest().getRequestURI();
                MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_IO_EXCEPTION;
                logger.error(new ErrorMessage(requestURI,errorEnum.getCode(),errorEnum.getMsg()+", 执行HTTP Post " + URLString + " 请求时，发生异常！", ErrorType.CustomerError).toString(),e);
            } finally {
                method.releaseConnection();
                fi.close();
            }
            return 1;
        } catch (Exception e) {
            return -1;
        }
    }

    //http://fp.baiwang.com/DownloadService/PublicDownLoad?filePath=templates/mobileTemplate.xlsx
//http://fp.baiwang.com/DownloadService/PublicDownLoad?filePath=templates/yunTemplate.xlsx
//http://fp.baiwang.com/DownloadService/PublicDownLoad?filePath=templates/syqzhdrmb.xlsx
//http://fp.baiwang.com/DownloadService/PublicDownLoad?filePath=templates/simplezhTemplate.xlsx
//https://fp.baiwang.com/DownloadService/PublicDownLoad?filePath=templates/simplezhTemplate.xlsx
//    public static void main(String args[]) {

//		#文件上传地址
//		uploadUrl =http://123.56.92.221/DownloadService/PublicUpLoad?fileFolder=
//		#文件下载地址
//		downloadUrl =http://123.56.92.221/DownloadService/PublicDownLoad?filePath=templates/importOrg.xlsx
//		access("http://123.56.92.221/DownloadService/PublicUpLoad?fileFolder=templates/importOrg.xlsx");//上传

//        access("https://fp.baiwang.com/DownloadService/PublicUpLoad?fileFolder=templates/einvoiceTemplate.xlsx");//上传
        //https://fp.baiwang.com/DownloadService/PublicDownLoad?filePath=templates/einvoiceTemplate.xlsx       //下载

        //https://fp.baiwang.com/DownloadService/PublicDownLoad?filePath=templates/yunTemplate.xlsx
        //http://123.56.92.221/DownloadService/PublicDownLoad?filePath=templates/importOrg.xlsx
//    }
}
