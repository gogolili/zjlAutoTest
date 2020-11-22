package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.Buffer;

public class FileMangerUtil {
	
	//递归删除文件
	private static void deleteFile(File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			//递归调用删除方法
			for (int i = 0; i < files.length; i++) {
				deleteFile(files[i]);
			}
		}
		file.delete();
	}
	
	//删除文件夹和密码
	public static void clearFile(String workspaceRootPath) {//文件目录
		File file = new File(workspaceRootPath);
		if (file.exists()) {
			deleteFile(file);
		}
	}
	
	//文件写入
	public void writeWithEncode(String path,String encode,boolean append,String content) {
		File file = new File(path);
		if (file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
			//以utf-8的格式构造一个文件输出流FileOutputStream，然后将这个文件输出字符流封装成字节输出流OutputStreamWriter，然后将这个字节输出流封装成缓冲字节输出流。
			BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,append),encode));
			bufferedWriter.write(content);
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//文件写入
	public void writeWithEncode(File file,String encode,boolean append,String content) {
		try {
			file.createNewFile();
			BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,append),encode));
			bufferedWriter.write(content);
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

}
