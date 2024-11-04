package org.example;
import lombok.Getter;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import java.util.ArrayList;
import java.util.List;
@Getter
public class CommunityManager {
    private List<Observer> observers;
    private SurveyCreatorManager surveyCreatorManager;

    public CommunityManager() {
        this.observers = new ArrayList<>();
        this.surveyCreatorManager = new SurveyCreatorManager(this);
    }

    private void addNewObserver(User user,TelegramLongPollingBot bot) {
        this.observers.add(user);
        this.newUserJoined(user);
        Utils.sendMessageToUser(user.getChatId(), "הצטרפת בהצלחה," + " הקהילה מונה "+observers.size()+" חברים", bot);
        Utils.sendMessageToUser(user.getChatId(), "אם אתה מעוניין ליצור סקר חדש תרשום 'סקר חדש'", bot);

    }
    public void processNewUser(User user, String messageText, TelegramLongPollingBot bot) {
        if (messageText.equalsIgnoreCase("הי") || messageText.equalsIgnoreCase("hi")) {
            if (!this.observers.contains(user)) {
                addNewObserver(user, bot);
            }
        }
        if(!observers.contains(user)) {
            String addToCommunity = "שלום אם אתה מעוניין להירשם לחברות בקהילת הסקרים תירשום הי או hi";
            Utils.sendMessageToUser(user.getChatId(), addToCommunity, bot);
        } else {
            this.surveyCreatorManager.handleUserMessage(user, messageText, bot);
        }
    }
// למקרה שנרצה להפעיל פונקציה זו
    public void removeObserver (Observer observer){
        this.observers.remove(observer);
    }

    public void notifyObservers (String message){
        if(this.observers.size() >1){
            for (int i = 0; i < this.observers.size()-1; i++) {
                this.observers.get(i).update(message);
            }
        }
    }
    private void newUserJoined (User newUser){
        String message = "יוזר " + newUser.getUsername() + " הצטרף לקהילת הסקרים " +
                "הקהילה מונה "+observers.size()+" חברים";
        notifyObservers(message);
    }
    public int getObserversCount(){
        return observers.size();
    }

}
