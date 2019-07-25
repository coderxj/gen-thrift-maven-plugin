package com.acme.plugins.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author acme
 * @date 2019/7/25 5:52 PM
 */
public class FileUtil {

    public static Map<String, String> searchFiles(File file, String pattern){
        Map<String, String> res = new HashMap<>();
        searchFiles(file, pattern, res);
        return res;
    }

    public static void searchFiles(File file, String pattern, Map<String, String> res){
        if(!file.exists()){
            return;
        } else {
            if(file.isFile() && file.getName().matches(pattern)){
                res.put(file.getName(), file.getPath());
            } else if(file.isDirectory()){
                File[] files = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    searchFiles(files[i], pattern, res);
                }
            }
        }
    }
}
