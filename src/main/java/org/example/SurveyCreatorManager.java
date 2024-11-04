
package org.example;

import lombok.Getter;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class SurveyCreatorManager {
    private ActiveSurveyManager activeSurveyManager = ActiveSurveyManager.getInstance();
    private List<String> surveyOptions;
    private String surveyQuestion;
    private List<SendPoll> createdSurveys;
    private int currentSurveyCount;
    private CommunityManager communityManager;
    private int delayInMinutes;
    private boolean waitingForDelay;
    private boolean waitingForSurveyToSend;
    @Getter
    private LocalDateTime timePollSentOut;
    private static final Map<String, SurveyDetails> pollsMap = Collections.synchronizedMap(new HashMap<>());

    public SurveyCreatorManager( CommunityManager communityManager) {
        this.surveyQuestion="";
        this.surveyOptions=new ArrayList<>();
        this.createdSurveys=new ArrayList<>();
        this.currentSurveyCount=0;
        this.communityManager = communityManager;
        this.waitingForDelay=false;
        this.waitingForSurveyToSend=false;
    }

    public void handleUserMessage(User user, String messageText, TelegramLongPollingBot bot) {
        if (this.waitingForSurveyToSend && messageText.equalsIgnoreCase("סקר חדש")) {
            Utils.sendMessageToUser(user.getChatId(), "אנא המתן, ישנו סקר הנמצא כרגע בעיבוד. לא ניתן ליצור סקר חדש עד שליחת הסקר הנוכחי.", bot);
            return;
        }
        if (this.waitingForDelay&&this.activeSurveyManager.getActiveUser().equals(user)) {
            handleDelayResponse(user, messageText, bot);
        } else if (!this.activeSurveyManager.isSurveyActive()) {
            if (messageText.equalsIgnoreCase("סקר חדש")) {
                if (this.communityManager.getObserversCount() >= Constants.MIN_OBSERVER_COUNT_FOR_SURVEY_CREATION) {
                    if (this.activeSurveyManager.startSurvey(user)) {
                        sendSurveyCreationPrompt(user, bot);
                    }
                    }else {
                    Utils.sendMessageToUser(user.getChatId(),
                            "כדי לרשום סקר חייב להיות בקהילה לפחות 3 רשומים כרגע יש " + this.communityManager.getObserversCount() + " רשומים אנא המתן להיצטרפות עוד חברים לקהילה", bot);
                }
            }
        } else if (this.activeSurveyManager.getActiveUser().equals(user)) {
            if(messageText.equalsIgnoreCase("סיום") && createdSurveys.size() >= Constants.MIN_SURVEYS) {//finish
                askForDelay(user, bot);
            } else {
                processSurveyCreation(user, messageText, bot);
            }
        } else if (!this.activeSurveyManager.getActiveUser().equals(user) && messageText.equalsIgnoreCase("סקר חדש")) {
            Utils.sendMessageToUser(user.getChatId(), "יוזר " + this.activeSurveyManager.getActiveUser().getUsername() + " יוצר כעת סקר. אנא המתן. ", bot);
        }
    }

    private void sendSurveyCreationPrompt(User user, TelegramLongPollingBot bot) {
        Utils.sendMessageToUser(user.getChatId(), "כעת אתה יוצר הסקר. נא להזין את שאלת הסקר:", bot);
    }

    private void processSurveyCreation(User user, String messageText, TelegramLongPollingBot bot) {
        if (this.surveyQuestion.isEmpty()) {
            this.surveyQuestion = messageText;
            Utils.sendMessageToUser(user.getChatId(), "השאלה שנישמרה היא: \"" + this.surveyQuestion + "\". כעת תירשום 2 עד 4 אפשרויות אחת אחת. הקלד 'בוצע' כשתסיים.", bot);
        } else if (messageText.equalsIgnoreCase("בוצע")) {
            if (this.surveyOptions.size() < Constants.MIN_OPTIONS) {
                Utils.sendMessageToUser(user.getChatId(), "חייבות להיות לך לפחות 2 אפשרויות. אנא הוסף אפשרויות נוספות.", bot);
            }else {
                createSurvey(user, bot);
                if (this.currentSurveyCount < Constants.MAX_SURVEYS) {
                    Utils.sendMessageToUser(user.getChatId(), "הסקר נוצר בהצלחה. אתה יכול ליצור סקר נוסף או להקליד 'סיום' כדי לסיים ולשלוח את הסקר.", bot);
                    Utils.sendMessageToUser(user.getChatId(), "ליצירת סקר נוסף,תרשום את שאלת הסקר",bot);
                    this.surveyQuestion = "";
                    this.surveyOptions.clear();
                } else {
                    Utils.sendMessageToUser(user.getChatId(), "הגעת למספר המרבי של סקרים.", bot);
                    askForDelay(user, bot);
                }
            }
        } else {
            if ((this.surveyOptions.size() < Constants.MAX_OPTIONS)) {
                if (!this.surveyOptions.contains(messageText)) {
                    this.surveyOptions.add(messageText);
                    Utils.sendMessageToUser(user.getChatId(), "נוספה אפשרות: \"" + messageText + "\".הזן אפשרות אחרת או הקלד 'בוצע' כדי לסיים.", bot);
                }
                else{
                    Utils.sendMessageToUser(user.getChatId(), "אופציה זו כבר קיימת,נא להוסיף אופציה אחרת", bot);
                }
            }    else {
                Utils.sendMessageToUser(user.getChatId(), "לא ניתן להוסיף יותר מ-4 אפשרויות. הקלד 'בוצע' כדי לסיים.", bot);
            }
        }
    }

    private void askForDelay(User user, TelegramLongPollingBot bot) {
        Utils.sendMessageToUser(user.getChatId(), "לאחר כמה דקות אתה רוצה לשלוח את הסקר לקהילה?", bot);
        this.waitingForDelay = true;
    }

    private void createSurvey(User user, TelegramLongPollingBot bot) {
        SendPoll poll = new SendPoll();
        poll.setChatId(user.getChatId().toString());
        poll.setQuestion(this.surveyQuestion);
        poll.setOptions(new ArrayList<>(this.surveyOptions));
        poll.setIsAnonymous(false);
        this.createdSurveys.add(poll);
        this.currentSurveyCount++;
        this.surveyOptions.clear();
    }

    public void handleDelayResponse(User user, String messageText, TelegramLongPollingBot bot) {
        try {
            this.waitingForSurveyToSend = true;
            this.delayInMinutes = Integer.parseInt(messageText);
            Utils.sendMessageToUser(user.getChatId(), "הסקר יישלח בעוד " + this.delayInMinutes + " דקות.", bot);
            finalizeSurveyCreation(user, bot);
        } catch (NumberFormatException e) {
                Utils.sendMessageToUser(user.getChatId(), "נא להזין מספר חוקי של דקות.", bot);
        } finally {
            this.waitingForDelay = false;
        }
    }

    private void finalizeSurveyCreation(User user, TelegramLongPollingBot bot) {
        new Thread(() -> {
            try {
                Thread.sleep(this.delayInMinutes * 60 * 1000);
                Utils.sendMessageToUser(user.getChatId(), "סיום יצירת הסקר. שולח סקרים לקהילה...", bot);
                Utils.sendMessageToUser(user.getChatId(), "המתן לקבלת תשובות ", bot);
                notifyObserversAboutSurveys(bot);
                this.surveyOptions.clear();
                this.surveyQuestion = "";
                this.currentSurveyCount = 0;
                this.waitingForSurveyToSend = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

private SendPoll pollCreation(SendPoll pollTemplate,Observer observer){
    SendPoll poll = new SendPoll();
    poll.setChatId(observer.getChatId().toString());
    poll.setQuestion(pollTemplate.getQuestion());
    poll.setOptions(pollTemplate.getOptions());
    poll.setIsAnonymous(pollTemplate.getIsAnonymous());
        return poll;
}

    private void notifyObserversAboutSurveys(TelegramLongPollingBot bot) {
        for (SendPoll pollTemplate : this.createdSurveys) {
            for (Observer observer : this.communityManager.getObservers()) {
                if (!observer.equals(this.activeSurveyManager.getActiveUser())) {
                    SendPoll poll=pollCreation(pollTemplate, observer);
                    try {
                        Message message = bot.execute(poll);
                        if (message.hasPoll()) {
                            String pollId = message.getPoll().getId();
                            synchronized (this.pollsMap) {
                                this.pollsMap.put(pollId, new SurveyDetails(poll.getQuestion(), poll.getOptions()));
                                this.timePollSentOut = LocalDateTime.now();
                            }
                        } else {
                            System.out.println("Poll sending failed to " + observer.getChatId());
                        }
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        this.createdSurveys.clear();
    }

    public Map<String, SurveyDetails> getPollsMap() {
        synchronized (this.pollsMap) {
            return this.pollsMap;
        }
    }

    public void setTimePollSentOutNull(){
        this.timePollSentOut=null;
    }

    public SurveyDetails getSurveyDetailsByPollId(String pollId) {
        return this.pollsMap.get(pollId);
    }

    public boolean checkIfEnoughTimePassed(LocalDateTime localDateTime){
        boolean enoughTimePassed = false;
        if (this.timePollSentOut!= null) {
            if(Duration.between(this.timePollSentOut, localDateTime).toMinutes() >= Constants.MINUTES_TO_WAIT_TO_SEND_STATS){
                enoughTimePassed = true;
            }
        }
        return enoughTimePassed;
    }
}

