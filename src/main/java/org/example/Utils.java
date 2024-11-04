package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Utils {
    public static void sendMessageToUser(Long chatId, String messageText, TelegramLongPollingBot bot) {
        SendMessage message = new SendMessage(chatId.toString(), messageText);
        try {
            bot.execute(message);
            } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
