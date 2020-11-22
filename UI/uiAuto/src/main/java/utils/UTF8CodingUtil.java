package utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

import jodd.io.UnicodeInputStream;

public class UTF8CodingUtil {
	
	public static InputStream getInputStream(String file) throws Exception {
		FileInputStream fis=null;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
			throw new Exception("IO Stream Error");
		}
		return fis;		
	}
	
	
	
	public static InputStream getInputStreamWithoutBom(String file,String enc) {
		UnicodeInputStream stream = null;
		try {
			FileInputStream fis = new FileInputStream(file);
//			 stream = new UnicodeInputStream(fis, null);
			System.out.println("encoding:" + stream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return stream;
	}
	
	public static class UnicodeInputStream extends InputStream{
		PushbackInputStream internalIn;
		boolean isInited = false;
		String defaultEnc;
		String encoding;
		
		private static final int BOM_SIZE = 4;
		
		public UnicodeInputStream(InputStream in,String defaultEnc) {
			internalIn = new PushbackInputStream(in,BOM_SIZE);
			this.defaultEnc = defaultEnc;
		}	

		public String getDefaultEnc() {
			return defaultEnc;
		}
		
		protected void init() throws IOException{
			if (isInited) {
				return;
			}
			byte bom[] = new byte[BOM_SIZE];
	        int n, unread;
	        n = internalIn.read(bom, 0, bom.length);
	        
	        if ((bom[0] == (byte) 0x00) && (bom[1] == (byte) 0x00)
                    && (bom[2] == (byte) 0xFE) && (bom[3] == (byte) 0xFF)) {
                encoding = "UTF-32BE";
                unread = n - 4;
            } else if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE)
                    && (bom[2] == (byte) 0x00) && (bom[3] == (byte) 0x00)) {
                encoding = "UTF-32LE";
                unread = n - 4;
            } else if ((bom[0] == (byte) 0xEF) && (bom[1] == (byte) 0xBB)
                    && (bom[2] == (byte) 0xBF)) {
                encoding = "UTF-8";
                unread = n - 3;
            } else if ((bom[0] == (byte) 0xFE) && (bom[1] == (byte) 0xFF)) {
                encoding = "UTF-16BE";
                unread = n - 2;
            } else if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE)) {
                encoding = "UTF-16LE";
                unread = n - 2;
            } else {
                // Unicode BOM mark not found, unread all bytes
                encoding = defaultEnc;
                unread = n;
            }
	        if (unread > 0) {
				internalIn.unread(bom,(n-unread),unread);
			}
	        
	        isInited =true;
		}
		
		public void close() throws IOException{
			internalIn.close();
		}



		public String getEncoding() {
			return encoding;
		}

		@Override
		public int read() throws IOException {
			return internalIn.read();
		}
	}

}
