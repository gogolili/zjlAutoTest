package functions;

import utils.StringUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;

/**
 * 返回当前时间
 */
public class DateFunction implements Function {

    @Override
    public String execute(String[] args) {
        if (args.length==0 || StringUtil.isEmpty(args[0])){//如果参数位为空，则返回当前时间
            return String.format("%s",new Date().getTime());
        }else {
            return getCurrentDate("yyyy-MM-dd");//如果不为空，返回格式化后的当前时间

        }
    }

    private String getCurrentDate(String pattern){
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        String str = format.format(new Date());
        return str;
    }


    @Override
    public String getReferenceKey() {
        return "date";
    }
}
