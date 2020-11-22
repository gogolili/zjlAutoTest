package com.baiwang.moirai.utils;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;

/**
 * User: rizenguo
 * Date: 2014/10/29
 * Time: 15:23
 */
public class Signature {
		
	private static final Logger logger = LoggerFactory.getLogger(Signature.class);
    /**
     * 签名算法
     * @param o 要参与签名的数据对象
     * @return 签名
     * @throws IllegalAccessException
     */
    public static String getSign(Object o,String payKey) throws IllegalAccessException {
        ArrayList<String> list = new ArrayList<String>();
        Class cls = o.getClass();
        Field[] fields = cls.getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            if (f.get(o) != null && f.get(o) != "") {
                list.add(f.getName() + "=" + f.get(o) + "&");
            }
        }
        int size = list.size();
        String [] arrayToSort = list.toArray(new String[size]);
        Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < size; i ++) {
            sb.append(arrayToSort[i]);
        }
        String result = sb.toString();
        result += "key=" + payKey;
        logger.info("Sign Before MD5:" + result);
        result = MD5Util.getMD5(result).toUpperCase();
        logger.info("Sign Result:" + result);
        return result;
    }

    public static String getSign(Map<String,Object> map, String signKey, String signValue){
    	 logger.info("获取支付sign秘钥");

    	 if (StrUtils.isEmpty(signKey)||StrUtils.isEmpty(signValue)){
    	     return "必填参数为空";
         }
         if (map==null||map.size()==0){
             return "没有需要签名的参数";
         }
        List<String> mapList = new ArrayList<String>(map.keySet());
        Collections.sort(mapList);

        StringBuilder queryString = new StringBuilder();
        for (String key : mapList) {
            Object value = map.get(key);
            if (!StrUtils.isEmpty(key) && value!=null) {
                queryString.append(key).append("=").append(value.toString()).append("&");
            }
        }
        queryString.append(signKey).append("=").append(signValue);
        String result = queryString.toString();
        logger.info("========:"+result);
        result = MD5Util.getMD5(result).toUpperCase();
        logger.info("Sign Result:" + result);
        return result;
    }


}
