package org.example;

import lombok.AllArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Update;
@AllArgsConstructor
public class UpdateHandlerFactory {
    private CommunityManager communityManager;
    private SurveyCreatorManager surveyCreatorManager;
    private SurveyStatisticsManager surveyStatisticsManager;
    private SurveyBot bot;

    public UpdateHandler getHandler(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            return new TextUpdateHandler(this.communityManager, this.bot);
        } else if (update.hasPollAnswer()) {
            return new PollUpdateHandler(this.surveyCreatorManager, this.surveyStatisticsManager);
        }
        return null;
    }
}

