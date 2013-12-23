package twitchapplication;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;


public class MonitorUtility {
    
    private static MonitorUtility singleton;

    public static MonitorUtility getInstance() {
        if (singleton == null) {
            singleton = new MonitorUtility();
        }
        return singleton;
    }

    private MonitorUtility() {}
    
    
    public int[] windowLocation(){
        screenMath();
        
        return null;
    }
    
    
    private void screenMath(){
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gd = ge.getScreenDevices();
        System.out.println(gd.length);
    }
    
}
