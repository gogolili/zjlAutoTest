package utils;

import org.testng.Assert;

/**
 * 断言
 */
public class AssertUtil {

    //判断是否包含
    public static void contains(String source,String search){
        Assert.assertTrue(source.contains(search),
                String.format("期待'%'包含'%'，实际为不包含.",source,search));
    }
    //判断是否不包含
    public static void notContains(String source,String search){
        Assert.assertTrue(source.contains(search),
                String.format("期待'%'不包含'%'，实际为包含.",source,search));
    }


}
