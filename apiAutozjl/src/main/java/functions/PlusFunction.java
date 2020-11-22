package functions;

import java.math.BigDecimal;

/**
 * 相加
 */
public class PlusFunction implements Function{

    @Override
    public String execute(String[] args) {
        BigDecimal value=new BigDecimal(0);
        boolean isNotPoint=true;
        for (String numerial:args){
            //如果有某个参数带有“。”，即代表double类型，将返回带有小数点的值
            isNotPoint=isNotPoint&& !numerial.contains(".");
            value =value.add(new BigDecimal(numerial));
        }
        if (isNotPoint){
            return String.valueOf(value.longValue());
        }else {
            return String.valueOf(value);
        }
    }

    @Override
    public String getReferenceKey() {
        return "plus";
    }
}
