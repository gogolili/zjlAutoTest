package utils;

public class DecodeUtil {
    public static String decodeUnicode(String theString){
        char aChar;
        int len = theString.length();

        StringBuffer outBuffer = new StringBuffer(len);

        for (int x=0;x<len;){
            aChar = theString.charAt(x++);
            if (aChar == '\\'){
                aChar = theString.charAt(x++);//如果achar=\,找下一个字符
                if (aChar=='u'){
                    int value = 0;
                    for (int i=0;i<4;i++){
                        aChar = theString.charAt(x++);
                        switch (aChar){
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;

                        }
                    }
                }
            }

        }
        return theString;
    }

}
