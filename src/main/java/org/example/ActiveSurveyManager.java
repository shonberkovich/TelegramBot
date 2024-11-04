
package org.example;

public class ActiveSurveyManager {
    private static ActiveSurveyManager instance;
    private User activeUser;

    private ActiveSurveyManager() {
    }

    public static synchronized ActiveSurveyManager getInstance() {
        if (instance == null) {
            instance = new ActiveSurveyManager();
        }
        return instance;
    }

    public boolean isSurveyActive() {
        return activeUser != null;
    }

    public User getActiveUser() {
        return activeUser;
    }

    public boolean startSurvey(User user) {
        if (activeUser == null) {
            this.activeUser = user;
            return true;
        }
        return false;
    }

    public void endSurvey() {
        this.activeUser = null;
    }
}