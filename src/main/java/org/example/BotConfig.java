package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BotConfig {
    private String botToken;

    public BotConfig() {
      loadBotToken();
    }
    public void loadBotToken(){
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                return;
            }
            properties.load(input);
            this.botToken = properties.getProperty("BOT_TOKEN");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String getBotToken() {
        return botToken;
    }
}

