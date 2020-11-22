package utils;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import jxl.Cell;
import jxl.CellType;
import jxl.DateCell;
import jxl.Sheet;
import jxl.Workbook;

public class ExcelReadUtil {
	public static Object[][] case_data_excel(int sheet_id, // sheet表，从0开始
											 int start_row, // 开始行，从0开始
											 int end_row, // 结束行，从0开始
											 int start_col, // 开始行，从0开始
											 int end_col, // 结束行，从0开始
											 String sourcefile// excel文件路径
	) {
		String cell_value = null;// 定义单元格的值为null
		Cell cell = null;// 定义单元格
		ArrayList<Object> testcase_data_list = new ArrayList<Object>();// 测试用例数组
		String[][] testcase_data_array = new String[end_row - start_row + 1][end_col - start_col + 1];// 测试用例各格的数据坐标
		Workbook testcase_data_workbook = null;// 定义了excel工作表
		try {
			testcase_data_workbook = Workbook.getWorkbook(new File(sourcefile));// 获取excel工作表
			Sheet testcase_data_sheet = testcase_data_workbook.getSheet(sheet_id);// 获取excel表的sheet
			int rows = testcase_data_sheet.getRows();// 获取行
			int cols = testcase_data_sheet.getColumns();// 获取列
			if (end_row - start_row + 1 > rows) {
				System.out.println("选择的行数超出实际数据范围内");
			}
			if (end_col - start_col + 1 > cols) {
				System.out.println("选择的列数超出实际数据范围内");
			}
			if (end_row > rows - 1) {
				System.out.println("行标超出实际行数数");
			}
			if (end_col > cols - 1) {
				System.out.println("列标超出实际列数数");
			}
			// 获取每行用例数据
			for (int row = start_row, i = 0; row <= end_row || i < testcase_data_array.length; row++, i++) {
				// 用一个数组，存放每行数据。
				// 每循环一行，初始化一次数组，将原有数组内存释放
				// 特别注意，只取一个表里的几列数据的时候，数组的长度一定要初始化正确
				String[] row_array = new String[end_col - start_col + 1];// 用一个数组，存放每行数据。每行的数据就是第n行的第一列到最后一列的数据
				for (int col = start_col, j = 0; col <= end_col || j < row_array.length; col++, j++) {
					cell = testcase_data_sheet.getCell(col, row);
					if (cell.getType() == CellType.DATE) {// 如果是时间格式
						DateCell dc = (DateCell) cell;// 将cell的类型改为时间格式的单元格DateCell
						Date date = dc.getDate();// 获取单元格的date类型
						cell_value = formatDate(date, "yyyy-MM-d");// 将date的格式转化为yyyy-MM-d格式
					} else {
						cell_value = testcase_data_sheet.getCell(col, row).getContents();// 如果不是时间格式，获取单元格的值
					}
					row_array[j] = cell_value;// 将每一行的每一个列值复制给行数组，循环行数组赋值
				}
				testcase_data_list.add(row_array);// 将获得一行数据就将其存入，用例List列表中
			}
			String[][] testcase_data_array_try = new String[testcase_data_list.size()][end_col - start_col + 1];// 行数大小，列数大小
			testcase_data_array_try = testcase_data_list.toArray(testcase_data_array_try);// 将list直接转为Object[] 数组
			testcase_data_array = testcase_data_array_try;

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		Object[][] testcase_data_object = (Object[][]) testcase_data_array;// 将数组转化为object类型
		return testcase_data_object;

	}

	public static Object[][] case_data_excel(int sheet_id, // sheet表，从0开始
											 int start_row, // 开始行，从0开始
											 int end_row, // 结束行，从0开始
											 int start_col, // 开始行，从0开始
											 int end_col, // 结束行，从0开始
											 InputStream SourceInputStream
											 ) 
	{
		String cell_value = null;// 定义单元格的值为null
		Cell cell = null;// 定义单元格
		ArrayList<Object> testcase_data_list = new ArrayList<Object>();// 测试用例数组
		String[][] testcase_data_array = new String[end_row - start_row + 1][end_col - start_col + 1];// 测试用例各格的数据坐标
		Workbook testcase_data_workbook = null;// 定义了excel工作表
		try {
			testcase_data_workbook = Workbook.getWorkbook(SourceInputStream);// 获取excel工作表
			Sheet testcase_data_sheet = testcase_data_workbook.getSheet(sheet_id);// 获取excel表的sheet
			int rows = testcase_data_sheet.getRows();// 获取行
			int cols = testcase_data_sheet.getColumns();// 获取列
			if (end_row - start_row + 1 > rows) {
				System.out.println("选择的行数超出实际数据范围内");
			}
			if (end_col - start_col + 1 > cols) {
				System.out.println("选择的列数超出实际数据范围内");
			}
			if (end_row > rows - 1) {
				System.out.println("行标超出实际行数数");
			}
			if (end_col > cols - 1) {
				System.out.println("列标超出实际列数数");
			}
			// 获取每行用例数据
			for (int row = start_row, i = 0; row <= end_row || i < testcase_data_array.length; row++, i++) {
				// 用一个数组，存放每行数据。
				// 每循环一行，初始化一次数组，将原有数组内存释放
				// 特别注意，只取一个表里的几列数据的时候，数组的长度一定要初始化正确
				String[] row_array = new String[end_col - start_col + 1];// 用一个数组，存放每行数据。每行的数据就是第n行的第一列到最后一列的数据
				for (int col = start_col, j = 0; col <= end_col || j < row_array.length; col++, j++) {
					cell = testcase_data_sheet.getCell(col, row);
					if (cell.getType() == CellType.DATE) {// 如果是时间格式
						DateCell dc = (DateCell) cell;// 将cell的类型改为时间格式的单元格DateCell
						Date date = dc.getDate();// 获取单元格的date类型
						cell_value = formatDate(date, "yyyy-MM-d");// 将date的格式转化为yyyy-MM-d格式
					} else {
						cell_value = testcase_data_sheet.getCell(col, row).getContents();// 如果不是时间格式，获取单元格的值
					}
					row_array[j] = cell_value;// 将每一行的每一个列值复制给行数组，循环行数组赋值
				}
				testcase_data_list.add(row_array);// 将获得一行数据就将其存入，用例List列表中
			}
			String[][] testcase_data_array_try = new String[testcase_data_list.size()][end_col - start_col + 1];// 行数大小，列数大小
			testcase_data_array_try = testcase_data_list.toArray(testcase_data_array_try);// 将list直接转为Object[] 数组
			testcase_data_array = testcase_data_array_try;

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		Object[][] testcase_data_object = (Object[][]) testcase_data_array;// 将数组转化为object类型
		return testcase_data_object;

	}

	// 将date格式化成指定的时间格式
	private static String formatDate(Date date, String format) {
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(date).toString();

	}
	
	public static void main(String[] args) {
//		String filepath="src/main/resources/data/userData.xls";
		String filepath = ExcelReadUtil.class.getResource("/data/userData.xls").getFile();
		System.out.println(filepath);
		Object[][] excel = ExcelReadUtil.case_data_excel(0, 1, 2, 0, 3, filepath);
		for (int i = 0; i < excel.length; i++) {
			Object[] excel2=excel[i];
			for (int j = 0; j < excel2.length; j++) {
				System.out.println(excel2[j]);
			}
		}
	}

}
