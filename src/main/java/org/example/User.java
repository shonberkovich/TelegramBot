package org.example;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

@AllArgsConstructor
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User implements Observer {
    private String username;
    @EqualsAndHashCode.Include
    private Long chatId;
    private TelegramLongPollingBot bot;

    @Override
    public void update(String message) {
        Utils.sendMessageToUser(this.chatId, message,this.bot);
    }
}