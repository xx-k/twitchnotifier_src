package twitchapplication;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MessageManager {

    private TwitchController ctr;
    private TwitchView viw;
    private ArrayList<QueuedMessage> messageQueue;
    private Timer fadeTimer, messageCheckTimer;
    
    private int fadeTimerCheck = 10;

    public MessageManager(TwitchController ctr) {
        this.ctr = ctr;
        setupQueue();
        setupTimers();
    }

    public boolean queueMessage(MessageState state, String message) {
       //return queueMessage(state, message, 3000);
        showMessage(255, new QueuedMessage(state, message));
        return true;
    }

    public boolean queueMessage(MessageState state, String message, int displayTime) {
        if (moduleReady()) {
            return addMessageToQueue(new QueuedMessage(state, message));
        }
        return false;
    }

    public void setView(TwitchView twv) {
        this.viw = twv;
    }

    private boolean addMessageToQueue(QueuedMessage messageToQueue) {
        return messageQueue.add(messageToQueue);
    }

    /**
     * *START MESSAGE CLASS SECTION**
     */
    private class QueuedMessage {

        public MessageState state;
        public String message;
        public int displayTime = 3000;
        public boolean displayed = false;

        public QueuedMessage(MessageState state, String message) {
            this.state = state;
            this.message = message;
            displayTime = 4000;
        }

        public QueuedMessage(MessageState state, String message, int displayTime) {
            this.state = state;
            this.message = message;
            this.displayTime = displayTime;
        }

        public void markAsDisplayed() {
            displayed = true;
        }
    }

    /**
     * *END MESSAGE CLASS SECTION**
     */
    
    
    /**
     * *START TIMER SECTION***
     */
    private QueuedMessage blankMessage = new QueuedMessage(MessageState.BLANK, "");
    private class CheckQueueTask extends TimerTask {
        @Override
        public void run() {
            if(!moduleReady()) {
                viw.showMessage(MessageState.ERROR, "Internal Error: Could not start message queuer!");
            }
            if (!messageQueue.isEmpty() && readyForNew) {
                fadeTimer = new Timer();
                fadeTimer.scheduleAtFixedRate(new ChangeFadeLevel(true, messageQueue.get(0)), 0, 5);
                messageQueue.remove(0);
                readyForNew = false;
            }
        }
    }

    private boolean readyForNew = true;

    private void setupTimers() {
        messageCheckTimer = new Timer();
        messageCheckTimer.scheduleAtFixedRate(new CheckQueueTask(), 0, 2000);
    }
    
    
    
    private class ChangeFadeLevel extends TimerTask {
        private boolean fadeIn;
        private int fadeLevel = 0;
        private QueuedMessage fadeMessage;
        public ChangeFadeLevel(boolean fadeIn, QueuedMessage fadeMessage) {fadeLevel = (fadeIn ? 0 : 255); this.fadeIn = fadeIn; this.fadeMessage = fadeMessage;}
        @Override
        public void run() {
            showMessage((fadeIn ? ++fadeLevel : fadeLevel--), fadeMessage);
            if(fadeIn && fadeLevel == 255) {
                startTimeout(fadeMessage);
            } else if(!fadeIn && fadeLevel == 0) {
                readyForNew = true;
                cancel();
            }
        }
    }
    
    private void startTimeout(final QueuedMessage message) {
        fadeTimer.cancel();
        fadeTimer = new Timer();
        Timer timeoutTimer = new Timer();
        timeoutTimer.schedule(new TimerTask(){
            @Override
            public void run() {
                fadeTimer.scheduleAtFixedRate(new ChangeFadeLevel(false, message), 0, 5);
            }
        }, message.displayTime);
    }

    /**
     * *END TIMER SECTION***
     */
    
    /**
     * *VIEW HANDLER START**
     */
    
    private void showMessage(int fadeLevel, QueuedMessage queuedMessage) {
       viw.showMessage(fadeLevel, queuedMessage.state, queuedMessage.message);
    }

    /**
     * *VIEW HANDLER END**
     */
    private void setupQueue() {
        messageQueue = new ArrayList<>();
    }

    private boolean moduleReady() {
        assert (viw != null);
        assert (messageQueue != null);
        assert (fadeTimer != null);
        assert (messageCheckTimer != null);
        return true;
    }
}
