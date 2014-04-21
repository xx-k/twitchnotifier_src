package twitchapplication;

import java.util.Timer;
import java.util.TimerTask;

public class MessageManager {

    private TwitchController ctr;
    private TwitchView viw;
    private Timer fadeTimer, waitTimer;
    
    public MessageManager(TwitchController ctr) {
        this.ctr = ctr;
    }

    
    private boolean fadeIn = true;
    private boolean haltFade = false;
    private int x = 0;
    public boolean queueMessage(final MessageState state, final String message) {
        //return queueMessage(state, message, 3000);
        //showMessage(255, new QueuedMessage(state, message));
        
                viw.showMessage(255, state, message);
                return true;

//        fadeTimer = new Timer();
//        waitTimer = new Timer();
//        fadeTimer.scheduleAtFixedRate(new TimerTask(){
//            @Override
//            public void run() {
//                if(haltFade) return;
//                viw.showMessage((fadeIn ? x++ : x--), state, message);
//                if(x == 254) {
//                    System.out.println("halting fade, starting wait");
//                    haltFade = true;
//                    waitTimer.schedule(new WaitTask(), 2000);
//                } else if (x == 1 && !fadeIn) {
//                    System.out.println("fade OUT done, canceling, preparing for next fade in");
//                    fadeIn = true;
//                    fadeTimer.cancel();
//                    fadeTimer.purge();
//                    return;
//                }
//            }
//        }, 0, 10);
//        return true;
    }
    
    private class WaitTask extends TimerTask {
        @Override
        public void run() {
            System.out.println("wait over, starting fade OUT");
            haltFade = false;
            fadeIn = false;
        }
    }
    
    public void setView(TwitchView twv) {
        this.viw = twv;
    }
}
