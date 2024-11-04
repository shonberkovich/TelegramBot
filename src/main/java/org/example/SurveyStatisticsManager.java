package org.example;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import java.time.LocalDateTime;
import java.util.*;

public class SurveyStatisticsManager {
    private Map<String, Map<String, Integer>> surveyResponsesMap;
    private Map<Long, Set<String>> userResponsesMap;
    private Map<String, SurveyDetails> pollsMap;
    private CommunityManager communityManager;
    private static Object lock = new Object();



    public SurveyStatisticsManager(Map<String, SurveyDetails> pollsMap, CommunityManager communityManager) {
        this.surveyResponsesMap=new HashMap<>();
        this.userResponsesMap=new HashMap<>();
        this.pollsMap = pollsMap;
        this.communityManager = communityManager;
        checkIfEnoughTimePassedToGatherStatistics();
    }
    private synchronized void  checkIfEnoughTimePassedToGatherStatistics() {
        new Thread(()->{
            while (true) {
                synchronized (lock) {
                    LocalDateTime localDateTime=LocalDateTime.now();
                    if(this.communityManager.getSurveyCreatorManager().checkIfEnoughTimePassed(localDateTime)) {
                        sendStatisticsToCreator();
                        resetStatistics();
                        ActiveSurveyManager.getInstance().endSurvey();
                        this.communityManager.getSurveyCreatorManager().setTimePollSentOutNull();
                    }
                }
            }
        }).start();
    }


    public void recordAnswer(String question, String option, Long userId) {
        this.surveyResponsesMap.putIfAbsent(question, new HashMap<>());
        this.userResponsesMap.putIfAbsent(userId, new HashSet<>());
        // שמירת התשובות
        Map<String, Integer> responses = this.surveyResponsesMap.get(question);
        responses.put(option, responses.getOrDefault(option, 0) + 1);
        // שמירת השאלות שעליהן המשתמש ענה
        this.userResponsesMap.get(userId).add(question);
        if (allObserversAnsweredAllQuestions()) {
            sendStatisticsToCreator();
            resetStatistics();
            ActiveSurveyManager.getInstance().endSurvey();

        }
    }

    // בודק אם כל המשתתפים השיבו על כל השאלות
    private boolean allObserversAnsweredAllQuestions() {
        int totalObservers = this.communityManager.getObserversCount() - 1; // כמות המשתתפים ללא יוצר הסקר
        int totalQuestions = 0;
        Set<String> questions = new HashSet<>();
        for (SurveyDetails surveyDetails : this.pollsMap.values()) {
            questions.add(surveyDetails.getQuestion());
        }
        totalQuestions = questions.size();
        for (Set<String> answeredQuestions : this.userResponsesMap.values()) {
            if (answeredQuestions.size() < totalQuestions) {
                return false; // ישנם משתמשים שעדיין לא ענו על כל השאלות
            }
        }
        return this.userResponsesMap.size() >= totalObservers;
    }

private void sendStatisticsToCreator() {
    Long currentActiveSurveyManagerId = ActiveSurveyManager.getInstance().getActiveUser().getChatId();
    TelegramLongPollingBot currentActiveSurveyManagerBot = ActiveSurveyManager.getInstance().getActiveUser().getBot();
    Set<String> processedQuestions = new HashSet<>();
    StringBuilder fullStatisticsMessage = new StringBuilder();
    List<String> statisticsMessages=new ArrayList<>();

    for (String pollId : this.pollsMap.keySet()) {
        SurveyDetails surveyDetails = this.pollsMap.get(pollId);
        String question = surveyDetails.getQuestion();
        Map<String, Integer> responses = this.surveyResponsesMap.get(question);
        if (responses == null || responses.isEmpty()) {
            if (!processedQuestions.contains("noResponse-" + question)) {
                fullStatisticsMessage.append("אף אחד לא ענה על השאלה: ").append(question).append("\n");
                processedQuestions.add("noResponse-" + question);
            }
            processedQuestions.add(question);
            continue;
        }
        int totalResponses = totalResponses(responses);
        String statisticsMessage = statisticsMessageBuilder(surveyDetails, responses, totalResponses);
        statisticsMessages.add(statisticsMessage);
        if(!processedQuestions.contains(question)) {
            fullStatisticsMessage.append(statisticsMessage).append("\n");
            processedQuestions.add(question);
        }
    }
    if (fullStatisticsMessage.length() > 0) {
        try {
            Utils.sendMessageToUser(currentActiveSurveyManagerId, fullStatisticsMessage.toString(), currentActiveSurveyManagerBot);
        } catch (Exception e) {
            System.out.println("Error occurred while sending message:");
            e.printStackTrace();
        }
    }
}

    private int totalResponses(Map<String, Integer> responses){
        return responses.values().stream().mapToInt(Integer::intValue).sum();
    }

private String statisticsMessageBuilder(SurveyDetails surveyDetails, Map<String, Integer> responses, int totalResponses) {
    StringBuilder statisticsMessage = new StringBuilder("תוצאות עבור שאלה: " + surveyDetails.getQuestion() +
            "\n" + "סך תשובות שניקלטו: " + totalResponses + "\n");
    List<Map.Entry<String, Double>> optionsWithPercentages = new ArrayList<>();
    for (String option : surveyDetails.getOptions()) {
        int count = responses.getOrDefault(option, 0);
        double percentage = (totalResponses > 0) ? (count / (double) totalResponses) * 100 : 0.0;
        optionsWithPercentages.add(new AbstractMap.SimpleEntry<>(option, percentage));
    }
    optionsWithPercentages.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));
    optionsWithPercentages.forEach(entry -> {
        statisticsMessage.append(String.format("%s: %.2f%%\n", entry.getKey(), entry.getValue()));
    });
    return statisticsMessage.toString();
}

    private void resetStatistics() {
        this.surveyResponsesMap.clear();
        this.userResponsesMap.clear();
        this.pollsMap.clear();
    }
}