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

import com.acme.plugins.util.Constants;
import com.acme.plugins.util.FileUtil;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.*;
import java.util.Map;


@Mojo(name = "run")
public class GenThrift extends AbstractMojo {

    @Parameter(property = "run.currentPath")
    private String currentPath;
    @Parameter(property = "run.outputPath")
    private String outputPath;

    public void execute() {
        init();

        if(!outputPathIsExist()){
            exec("mkdir -p " + outputPath);
        }

        File file = new File(currentPath);

        outputConfig();

        System.out.println("[INFO]【开始】正在生成java文件");
        long b = System.currentTimeMillis();

        Map<String, String> filesMap = FileUtil.searchFiles(file, ".*\\.thrift");

        int fileNums = genThrift(filesMap);

        if(fileNums == 0)
            System.out.println("[INFO]【结束】请检查当前目录下是否有thrift文件");
        else
            System.out.println("[INFO]【结束】java文件已生成至目录" + outputPath);
        System.out.println("[INFO]【耗时】" + (System.currentTimeMillis() - b) / 1000.0 + " s");

    }

    private void init(){
        if(currentPath == null || "".equals(currentPath))
            currentPath = Constants.DEFUALT_CURRENT_PATH;
        if(outputPath == null || "".equals(outputPath))
            outputPath = Constants.DEFUALT_OUTPUT_PATH;
        else
            outputPath = currentPath + outputPath;
    }

    private void outputConfig(){
        System.out.println("[INFO]【配置信息】");
        System.out.println("[INFO] " + "currentPath" + "=" + currentPath);
        System.out.println("[INFO] " + "outputPath" + "=" + outputPath);
    }

    private int genThrift(Map<String, String> filesMap){
        System.out.println("---------------------------------------------");
        int i = 0;
        for (Map.Entry<String, String> entry : filesMap.entrySet()){
            System.out.println(++i + " -- " + entry.getKey());
            exec("thrift -out " + outputPath + " -gen java " + entry.getValue());
        }
        System.out.println("---------------------------------------------");
        return i;
    }

    private boolean outputPathIsExist(){
        File file = new File(outputPath);
        return file.exists() && file.isDirectory();
    }

    public void exec(String cmd) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
            InputStream in = process.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            for(String line = br.readLine(); line != null; line = br.readLine()) {
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
