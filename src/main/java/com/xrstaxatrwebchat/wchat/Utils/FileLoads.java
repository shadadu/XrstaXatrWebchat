package com.xrstaxatrwebchat.wchat.Utils;

import javax.naming.Context;

public class FileLoads {

    public String toTempPath(String file, Context context) throws Exception {

        String srcPath = (String) context.lookup("java:comp/env/resourcesPath");

        String path = srcPath + file;
        System.out.println("File path: "+ path);

        return path;

    }
}
