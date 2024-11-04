package org.example;

import lombok.AllArgsConstructor;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

@AllArgsConstructor
    public class UserCreator {
    private String username;
    private Long chatId;
    private TelegramLongPollingBot bot;

    //Add logic as needed
    public User buildUser (){
        return new User(this.username, this.chatId, this.bot);
    }
}

