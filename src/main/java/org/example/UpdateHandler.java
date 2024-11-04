package org.example;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateHandler {
    void handleUpdate(Update update);
}
