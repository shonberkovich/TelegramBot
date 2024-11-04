package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public class SurveyBot extends TelegramLongPollingBot {
    private CommunityManager communityManager;
    private SurveyCreatorManager surveyCreatorManager;
    private SurveyStatisticsManager surveyStatisticsManager;
    private UpdateHandlerFactory handlerFactory;
    private BotConfig botConfig;
    public SurveyBot() {
        this.botConfig = new BotConfig();
        this.communityManager = new CommunityManager();
        this.surveyCreatorManager = new SurveyCreatorManager(communityManager);
        this.surveyStatisticsManager = new SurveyStatisticsManager(surveyCreatorManager.getPollsMap(), communityManager);
        this.handlerFactory = new UpdateHandlerFactory(communityManager, surveyCreatorManager, surveyStatisticsManager, this);
    }

    @Override
    public String getBotUsername() {
        return "Shai2024";
    }
    //return your bot token instead
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
     processUpdate(update);
    }

    private void processUpdate(Update update) {
        UpdateHandler handler = handlerFactory.getHandler(update);
        if (handler != null) {
            handler.handleUpdate(update);
        } else {
            System.out.println("No handler found for the given update.");
        }
    }
}
