package functions;

import java.math.BigDecimal;

public class MaxFunction implements Function {

    /**
     * 返回传参中的最大值
     * @param args
     * @return
     */
    @Override
    public String execute(String[] args) {
// Java在java.math包中提供的API类BigDecimal，用来对超过16位有效位的数进行精确的运算。
// 双精度浮点型变量double可以处理16位有效数，但在实际应用中，可能需要对更大或者更小的数进行运算和处理。
// 一般情况下，对于那些不需要准确计算精度的数字，我们可以直接使用Float和Double处理，但是Double.valueOf(String) 和Float.valueOf(String)会丢失精度。
// 所以开发中，如果我们需要精确计算的结果，则必须使用BigDecimal类来操作。

//        创建一个具有参数所指定整数值的对象
        BigDecimal maxValue = new BigDecimal(args[0]);
        for (String numerial:args){
            maxValue = maxValue.max(new BigDecimal(numerial));
        }
        return String.valueOf(maxValue);
    }

    @Override
    public String getReferenceKey() {
        return "max";
    }
}
