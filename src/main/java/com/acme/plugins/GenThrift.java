package com.acme.plugins;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @goal run
 * @phase process-sources
 */
public class GenThrift extends AbstractMojo {

    private final String DEFUALT_CURRENT_PATH = System.getProperty("user.dir");
    private final String DEFUALT_ORIGINAL_OUTPUT_PATH = DEFUALT_CURRENT_PATH + "/gen-java/";
    private final String DEFUALT_NEW_OUTPUT_PATH = DEFUALT_CURRENT_PATH + "/src/main/java";

    private String CURRENT_PATH = DEFUALT_CURRENT_PATH;
    private String ORIGINAL_OUTPUT_PATH = DEFUALT_ORIGINAL_OUTPUT_PATH;
    private String NEW_OUTPUT_PATH = DEFUALT_NEW_OUTPUT_PATH;
    private String NO_WARNNING = "FALSE";

    private Map<String, String> config = new HashMap<String, String>(){{
        put("CURRENT_PATH", CURRENT_PATH);
        put("ORIGINAL_OUTPUT_PATH", ORIGINAL_OUTPUT_PATH);
        put("NEW_OUTPUT_PATH", NEW_OUTPUT_PATH);
        put("NO_WARNNING", NO_WARNNING);
    }};

    public void execute() {
        File file = new File(config.get("CURRENT_PATH"));

        readConfig(file);

        outputConfig();

        System.out.println("[INFO]【开始】正在生成java文件");
        long b = System.currentTimeMillis();

        Map<String, String> filesMap = searchFiles(file, ".*\\.thrift");

        int fileNums = genThrift(filesMap);

        moveAndDeleteFiles();

        if(fileNums == 0)
            System.out.println("[INFO]【结束】请检查当前目录下是否有thrift文件");
        else
            System.out.println("[INFO]【结束】java文件已生成至目录" + config.get("NEW_OUTPUT_PATH"));
        System.out.println("[INFO]【耗时】" + (System.currentTimeMillis() - b) / 1000.0 + " s");

    }

    private void readConfig(File file){
        Map<String, String> res = searchFiles(file, ".*\\.tconfig");
        for (Map.Entry<String, String> entry : res.entrySet()){
            paserConfigFile(entry.getValue());
            break;
        }
    }

    private void paserConfigFile(String path){
        File f = new File(path);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(f));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] cf = paserConfigLine(line);
                if(cf != null && config.containsKey(cf[0].toUpperCase())){
                    config.put(cf[0].toUpperCase(), CURRENT_PATH + cf[1]);
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            if(reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }
    }

    private String[] paserConfigLine(String line){
        if(line.trim().toLowerCase().matches("thrift\\.config\\..*=.*")){
            Pattern p = Pattern.compile("thrift\\.config\\.(.*)=(.*)");
            Matcher matcher = p.matcher(line.trim().toLowerCase());
            if(matcher.find()){
                return new String[]{matcher.group(1), matcher.group(2)};
            }
        }
        return null;
    }

    private void outputConfig(){
        System.out.println("[INFO]【配置信息】");
        for (Map.Entry<String, String> entry : config.entrySet()){
            System.out.println("[INFO] " + entry.getKey().toLowerCase() + "=" + entry.getValue().toLowerCase());
        }
    }

    private Map<String, String> searchFiles(File file, String pattern){
        Map<String, String> res = new HashMap<>();
        searchFiles(file, pattern, res);
        return res;
    }

    private void searchFiles(File file, String pattern, Map<String, String> res){
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

    private int genThrift(Map<String, String> filesMap){
        System.out.println("---------------------------------------------");
        int i = 0;
        for (Map.Entry<String, String> entry : filesMap.entrySet()){
            System.out.println(++i + " -- " + entry.getKey());
            exec("thrift -gen java " + entry.getValue());
        }
        System.out.println("---------------------------------------------");
        return i;
    }

    private void moveAndDeleteFiles(){
        File[] files = (new File(config.get("NEW_OUTPUT_PATH"))).listFiles();
        if (files != null && files.length > 0) {
            exec("rm -r " + config.get("NEW_OUTPUT_PATH"));
        }

        exec("mv " + config.get("ORIGINAL_OUTPUT_PATH") + " " + config.get("NEW_OUTPUT_PATH"));
        exec("rm -r " + config.get("ORIGINAL_OUTPUT_PATH"));
    }

    public void exec(String cmd) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
            InputStream in = process.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            for(String line = br.readLine(); line != null; line = br.readLine()) {
                if(config.get("NO_WARNNING").equalsIgnoreCase("FALSE"))
                    System.out.println(line);
            }

        } catch (IOException e) {
            System.out.println(e);
        } finally {
            if (process != null){
                process.destroy();
            }
        }

    }
}
