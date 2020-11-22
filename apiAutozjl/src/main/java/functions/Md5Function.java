package functions;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class Md5Function implements Function {
    @Override
    public String execute(String[] args) {

            try {
                String filePath = args[0];
                if (filePath.startsWith("http")) {
                    return DigestUtils.md5Hex(new URL(filePath).openStream());
                }else {
                    return DigestUtils.md5Hex(new FileInputStream(new File(filePath)));
                }
        } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
    }

    @Override
    public String getReferenceKey() {
        return "md5";
    }
}
