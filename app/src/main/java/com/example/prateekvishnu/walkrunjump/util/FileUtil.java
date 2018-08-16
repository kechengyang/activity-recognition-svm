package com.example.prateekvishnu.walkrunjump.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by css on 2018/7/27.
 */

public class FileUtil {
    public static void write(String basePath, String fileName, String content, boolean isAppend) {
        File file = new File(basePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        File data = new File(basePath + "/" + fileName);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(data, isAppend);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
