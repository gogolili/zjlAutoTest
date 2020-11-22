package utils;

import javax.swing.colorchooser.ColorChooserComponentFactory;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class ClassFinder {
    static ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    /**
     * 获取同一路径下所有子类或接口实现类
     * @param cls
     * @return
     */
    public static List<Class<?>> getAllAssignedClass(Class<?> cls){
        List<Class<?>> classes = new ArrayList<Class<?>>();
        for (Class<?> c:getClasses(cls)){
            if (cls.isAssignableFrom(c) && !cls.equals(c)){
                classes.add(c);
            }
        }
        return classes;
    }

    /**
     * 取得当前类路径下的所有类
     * @param cls
     * @return
     */
    public static List<Class<?>> getClasses(Class<?> cls){
        String pk = cls.getPackage().getName();//获取当前类所在的包文件名,获取到的包名为XXX.CCC.CDD
        String path = pk.replace('.','/');//将获取到的包文件名中的.换程/

        try {
            String dirpath = URLDecoder.decode(classLoader.getResource(path).getPath(),"utf-8");//将获取到的文件路径转码
            return getClasses(new File(dirpath),pk);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return new ArrayList<Class<?>>();
    }

    /**
     * 迭代查找类，取得当前路径下的所有类
     * @param dir 文件夹
     * @param pk  包名
     * @return
     */
    public static List<Class<?>> getClasses(File dir,String pk){
        List<Class<?>> classes = new ArrayList<Class<?>>();
        if (!dir.exists()){
            return classes;
        }
        for (File file:dir.listFiles()){
            if (file.isDirectory()){
                classes.addAll(getClasses(file,pk+"."+ file.getName()));
            }
            String name = file.getName();
            if (name.endsWith(".class")){
                try {
                    classes.add(Class.forName(pk +"."+name.substring(0,name.length()-6)));//获取的名字去掉后6位即去掉.class
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return classes;

    }

}
