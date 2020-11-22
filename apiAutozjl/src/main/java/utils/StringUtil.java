package utils;

public class StringUtil {
    public static boolean isNotEmpty(String str){
        return null !=str && !"".equals(str);
    }

    public static boolean isEmpty(String str){
        return null == str ||"".equals(str);
    }

    /**
     *
     * @param sourceStr 待替换字符串,例如："QWEEE${rrr}EE"
     * @param matchStr  匹配字符串 例如："${rrr}"
     * @param replaceStr    目标替换字符串 例如："888"
     * @return
     */
    public static String replaceFirst(String sourceStr,String matchStr,String replaceStr){
        int index = sourceStr.indexOf(matchStr);
        int matLength = matchStr.length();
        int sourLength = sourceStr.length();

        String beginStr = sourceStr.substring(0,index);
        String endStr = sourceStr.substring(index+matLength,sourLength);
        sourceStr = beginStr + replaceStr + endStr;
        return sourceStr;
    }


    public static void main(String[] args) {
        String sourceStr = "QWEEE${rrr}EE";
        String matchStr = "${rrr}";
        String replaceStr = "888";
        String re = StringUtil.replaceFirst(sourceStr,matchStr,replaceStr);
        System.out.println(re);
    }
}
