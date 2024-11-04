package org.example;

import lombok.AllArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.polls.PollAnswer;
@AllArgsConstructor
public class PollUpdateHandler implements UpdateHandler {
    private SurveyCreatorManager surveyCreatorManager;
    private SurveyStatisticsManager surveyStatisticsManager;

    @Override
    public void handleUpdate(Update update) {
        PollAnswer pollAnswer = update.getPollAnswer();
        String pollId = pollAnswer.getPollId();
        SurveyDetails surveyDetails = this.surveyCreatorManager.getSurveyDetailsByPollId(pollId);
        if (surveyDetails != null) {
            Integer selectedOptionId = pollAnswer.getOptionIds().get(0);
            String selectedOption = surveyDetails.getOptions().get(selectedOptionId);
            Long userID = pollAnswer.getUser().getId();
            String question = surveyDetails.getQuestion();
            this.surveyStatisticsManager.recordAnswer(question, selectedOption, userID);
        } else {
            System.out.println("Poll not found for ID: " + pollId);
        }
    }
}
