package com.baiwang.moirai.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.baiwang.cloud.common.enumutil.ErrorType;
import com.baiwang.cloud.common.model.ErrorMessage;
import com.baiwang.moirai.common.WebContext;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

/**
 * @描述：测试excel读取 导入的jar包
 * <p>
 * poi-3.8-beta3-20110606.jar
 * <p>
 * poi-ooxml-3.8-beta3-20110606.jar
 * <p>
 * poi-examples-3.8-beta3-20110606.jar
 * <p>
 * poi-excelant-3.8-beta3-20110606.jar
 * <p>
 * poi-ooxml-schemas-3.8-beta3-20110606.jar
 * <p>
 * poi-scratchpad-3.8-beta3-20110606.jar
 * <p>
 * xmlbeans-2.3.0.jar
 * <p>
 * dom4j-1.6.1.jar
 * <p>
 * jar包官网下载地址：http://poi.apache.org/download.html
 * <p>
 * 下载poi-bin-3.8-beta3-20110606.zipp
 */

public class ImportExcel {

    private static final Logger logger = LoggerFactory.getLogger(ImportExcel.class);

    /**
     * 总行数
     */

    private int totalRows = 0;

    /**
     * 总列数
     */

    private int totalCells = 0;

    /**
     * 错误信息
     */

    private String errorInfo;

    /**
     * 构造方法
     */

    public ImportExcel() {

    }

    /**
     * @描述：得到总行数
     * @参数：@return
     * @返回值：int
     */

    public int getTotalRows() {

        return totalRows;

    }

    /**
     * @描述：得到总列数
     * @参数：@return
     * @返回值：int
     */

    public int getTotalCells() {

        return totalCells;

    }

    /**
     * @描述：得到错误信息
     * @参数：@return
     * @返回值：String
     */

    public String getErrorInfo() {

        return errorInfo;

    }

    /**
     * @描述：验证excel文件
     * @参数：@param filePath　文件完整路径
     * @参数：@return
     * @返回值：boolean
     */

    public boolean validateExcel(String filePath) {

        /** 检查文件名是否为空或者是否是Excel格式的文件 */

        if (filePath == null || !(this.isExcel2003(filePath) || this.isExcel2007(filePath))) {

            errorInfo = "文件名不是excel格式";

            return false;

        }

        /** 检查文件是否存在 */

        File file = new File(filePath);

        if (file == null || !file.exists()) {

            errorInfo = "文件不存在";

            return false;

        }

        return true;

    }

    /**
     * @描述：根据文件名读取excel文件
     * @参数：@param filePath 文件完整路径
     * @参数：@return
     * @返回值：List
     */

    public List<ArrayList<String>> read(MultipartFile file) throws IOException, InvalidFormatException {
//        List<ArrayList<String>> dataLst = new ArrayList<ArrayList<String>>();
//
//        InputStream is = null;
//        try {
//            is = file.getInputStream();
//            dataLst = read(is, false);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return dataLst;
//        }
        Workbook wb = WorkbookFactory.create(file.getInputStream());
        return this.read(wb);
    }

    /**
     * @描述：根据流读取Excel文件
     * @参数：@param inputStream
     * @参数：@param isExcel2003
     * @参数：@return
     * @返回值：List
     */

    public List<ArrayList<String>> read(InputStream inputStream, boolean isExcel2003) {

        List<ArrayList<String>> dataLst = null;

        try {

            /** 根据版本选择创建Workbook的方式 */

            Workbook wb = null;

            if (isExcel2003) {
                wb = new HSSFWorkbook(inputStream);
            } else {
                wb = new XSSFWorkbook(inputStream);
            }
            dataLst = read(wb);

        } catch (IOException e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_IO_EXCEPTION;
            logger.error(new ErrorMessage(requestURI,errorEnum.getCode(),errorEnum.getMsg(), ErrorType.CustomerError).toString(),e);
        }

        return dataLst;

    }

    /**
     * @描述：读取数据
     * @参数：@param Workbook
     * @参数：@return
     * @返回值：List<List<String>>
     */
    private List<ArrayList<String>> read(Workbook wb) {

        List<ArrayList<String>> dataLst = new ArrayList<ArrayList<String>>();

        /** 得到第一个shell */

        Sheet sheet = wb.getSheetAt(0);

        /** 得到Excel的行数 */

        this.totalRows = sheet.getPhysicalNumberOfRows();

        /** 得到Excel的列数 */

        if (this.totalRows >= 1 && sheet.getRow(1) != null) {

            this.totalCells = sheet.getRow(1).getPhysicalNumberOfCells();

        }

        /** 循环Excel的行 */

        for (int r = 1; r < this.totalRows; r++) {

            Row row = sheet.getRow(r);

            if (row == null) {

                continue;

            }

            ArrayList<String> rowLst = new ArrayList<String>();

            /** 循环Excel的列 */

            for (int c = 0; c < this.getTotalCells(); c++) {

                Cell cell = row.getCell(c);

                String cellValue = "";

                if (null != cell) {
                    // 以下是判断数据的类型
                    switch (cell.getCellType()) {
                        case NUMERIC: // 数字
                            DecimalFormat df = new DecimalFormat("#.##");
                            cellValue = df.format(cell.getNumericCellValue());
                            String strArr = cellValue.substring(cellValue.lastIndexOf(".") + 1, cellValue.length());
                            if (strArr.equals("00")) {
                                cellValue = cellValue.substring(0, cellValue.lastIndexOf("."));
                            }
                            break;

                        case STRING: // 字符串
                            cellValue = cell.getStringCellValue();
                            break;

                        case BOOLEAN: // Boolean
                            cellValue = cell.getBooleanCellValue() + "";
                            break;

                        case FORMULA: // 公式
                            cellValue = cell.getCellFormula() + "";
                            break;

                        case BLANK: // 空值
                            cellValue = "";
                            break;

                        case ERROR: // 故障
                            cellValue = "非法字符";
                            break;

                        default:
                            cellValue = "未知类型";
                            break;
                    }
                }

                rowLst.add(cellValue);

            }

            /** 保存第r行的第c列 */

            dataLst.add(rowLst);

        }

        return dataLst;
    }

    /**
     * @描述：是否是2003的excel，返回true是2003
     * @参数：@param filePath　文件完整路径
     * @参数：@return
     * @返回值：boolean
     */

    public static boolean isExcel2003(String filePath) {

        return filePath.matches("^.+\\.(?i)(xls)$");

    }

    /**
     * @描述：是否是2007的excel，返回true是2007
     * @参数：@param filePath　文件完整路径
     * @参数：@return
     * @返回值：boolean
     */

    public static boolean isExcel2007(String filePath) {

        return filePath.matches("^.+\\.(?i)(xlsx)$");

    }
}