package org.example.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {

    public static String get(String key){
        Properties prop = new Properties();

        try(FileInputStream input = new FileInputStream("C:\\Users\\LENOVO\\Desktop\\robot-3d-java\\config.properties")){
            prop.load(input);
            if(prop.getProperty(key) == null)System.out.println("fuck you");
            return prop.getProperty(key);
        } catch (IOException e) {
            System.out.println("Could not load config.properties!");
            e.printStackTrace();
            return null;
        }
    }
}
