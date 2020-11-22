package utils;

import org.testng.Reporter;

import java.util.Calendar;

public class ReportUtil {

    private static String reportName = "自动化测试报告";
    private static String splitTImeAndMsg =  "===";

    public static void log(String msg){
        long timeMillis = Calendar.getInstance().getTimeInMillis();
        Reporter.log(timeMillis + splitTImeAndMsg + msg,true);
    }

    public static String getReportName() {
        return reportName;
    }

    public static void setReportName(String reportName) {
        if (StringUtil.isEmpty(reportName)){
            ReportUtil.reportName = reportName;
        }

    }

    public static String getSpiltTimeAndMsg() {
        return splitTImeAndMsg;
    }

    public static void setSplitTImeAndMsg(String splitTImeAndMsg) {
        ReportUtil.splitTImeAndMsg = splitTImeAndMsg;
    }
}
