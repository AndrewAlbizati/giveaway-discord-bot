package com.github.AndrewAlbizati;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        // Create giveaways dir
        try {
            Files.createDirectories(Paths.get("giveaways"));
            System.out.println("giveaways directory has been created");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error creating giveaways directory!");
            return;
        }

        // Create config.properties
        try {
            File config = new File("config.properties");
            if (config.createNewFile()) {
                FileWriter writer = new FileWriter("config.properties");
                writer.write("token=");
                writer.close();
                System.out.println("config.properties has been created");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error creating config.properties");
            return;
        }

        // Get token from config.properties
        String token;
        try {
            Properties prop = new Properties();
            FileInputStream ip = new FileInputStream("config.properties");
            prop.load(ip);
            ip.close();

            token = prop.getProperty("token");

            if (token.length() == 0)
                throw new NullPointerException("Please add a Discord bot token into config.properties");
        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
            System.out.println("Token not found!");
            return;
        }

        Bot bot = new Bot(token);
        bot.start();
    }
}
