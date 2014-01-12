package twitchapplication;

import java.awt.*;
import java.util.*;
import javax.swing.*;

public class TwitchController {

    private final String twitchAPIVersion = "application/vnd.twitchtv.v2+json";
    private final String clientID = "q4dpzyshmhid2kjx5u0n6s34odf533k";
    private final String trayName = "Twitch Notifier";
    
    private String appVersion = "";


    
    public enum MessageState {
        INFO, WARNING, ERROR, BLANK;
    }
    
    private TwitchView twv;
    private ConfigWindow cfw;
    private JSONModel jsm;
    
    private boolean isLoggedIn = false;
    
    
    // params
    private boolean disableNotifications = false;
    private boolean autoLogin = false;
    private boolean startInTray = false;
    private boolean popoutVideos = false;
    private boolean startWithWindows = false;
    private boolean undecoratedWindow = false;
    private boolean resetLocation;
    private int timeout = 30;
    private String username = "";
    
    public TwitchController() {
        jsm = new JSONModel(this);
    }

    public String getTwitchAPIVersion() {
        return twitchAPIVersion;
    }

    public String getClientID() {
        return clientID;
    }
    
    /**
     * @param i 0 = error, 1 = info, 2 = blank, 3 = warning (exceptions)
     */
    public void showMessage(MessageState en, String msg) {
        twv.showMessage(en, msg);
    }
    
    public void allowUsernameToBeRemembered(boolean b){
        cfw.allowUsernameToBeRemembered(b);
    }
    
    public ArrayList<Streamer> generateOnlineList(String username) {
        return jsm.generateList(username);
    }

    public void fireUsername(String text) {
        this.username = text;
        twv.enableButton(false);
        ArrayList<Streamer> streamers = generateOnlineList(username);
        if (streamers != null) {
            isLoggedIn = true;
            try {
                showMessage(MessageState.BLANK, "");
                twv.setContentPanel(1);
            }
            catch (Exception ex) {
                twv.enableButton(true);
                showMessage(MessageState.WARNING, "Unable to show streamers.");
                ex.printStackTrace();
            }
            twv.generateContent(streamers);
        }
    }
    
    public TwitchView getView(){
        return twv;
    }
    
    public void setUsername(String un) {
        twv.setUsername(un);
    }

    public void update() {
        fireUsername(username);
    }

    public void setContentPanel(int i) {
        if(i==0) isLoggedIn = false;
        twv.setContentPanel(i);
    }
    
    public void toggleWindow(boolean b){
        twv.setVisible(b);
        showConfigWindow(false);
        twv.setConfigIcon(false);
    }
    
    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
        cfw.setAppVersion();
    }
    
    
    int i = 0;
    public void loadParams(HashMap<String, String> params){
        username = params.get("User");
        undecoratedWindow = Boolean.parseBoolean(params.get("UndecoratedWindow"));
        disableNotifications = Boolean.parseBoolean(params.get("DisableNotifications"));
        autoLogin = Boolean.parseBoolean(params.get("AutoLogin"));
        startInTray = Boolean.parseBoolean(params.get("StartMinimized"));
        popoutVideos = Boolean.parseBoolean(params.get("PopoutVideo"));
        timeout = Integer.parseInt(params.get("TimerUpdate"));
        int[] x = new int[2];
        if((params.get("RememberPosition").equals("true"))){
            try{
                x[0] = Integer.parseInt(params.get("PosX"));
                x[1] = Integer.parseInt(params.get("PosY"));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(twv, "Could not save position!");
                x[0] = 0;
                x[1] = 0;
            }
        } else  {
            x[0] = 0;
            x[1] = 0;
        }
        if(i++ == 0){
            startGui(x, params);
        } else {
            twv.setPopoutVideo(popoutVideos);
        }
    }
  
    public void enablePopoutLinks(boolean popout){
        twv.setPopoutVideo(popout);
    }
    
    private void startGui(int[] i, HashMap<String, String> map) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            twv.showMessage(MessageState.WARNING, ex.getClass().getName() + ": Could not set layout, using default UI");
        }
        JFrame.setDefaultLookAndFeelDecorated(true);
        twv = new TwitchView(this, undecoratedWindow);
        cfw = new ConfigWindow(this);
        if(i!=null){
            twv.setLocation(i[0], i[1]);
        } else {
            resetLocation();
        }
        windowParams();
        cfw.enterProperties(map);
        cfw.setVisible(false);
        if(startInTray){
            twv.minimizeWindow();
        } else {
            twv.setVisible(true);
        }
    }

    public void showOnline(ArrayList<Streamer> onlineStreamers) {
        if (!isLoggedIn) {
            trayNotify(MessageState.INFO, "Please log in first.");
            return;
        }
        StringBuilder sb;
        if (onlineStreamers == null || onlineStreamers.isEmpty()) {
            sb = new StringBuilder("No streamers online.");
        } else {
            sb = new StringBuilder("Following streamers are online:\n");
            for (Streamer str : onlineStreamers) {
                sb.append(str.getStreamerName()).append(" (" + str.getViewers() + ")").append("\n");
            }
        }
        trayNotify(MessageState.INFO, sb.toString());
    }

    public void windowParams(){
        if(autoLogin && !username.isEmpty()){
            twv.fireLogin();
        }
        twv.setTimeout(timeout);
        twv.setPopoutVideo(popoutVideos);
        cfw.setTimeout(timeout);
        twv.setUsername(username);
    }
    
    public boolean getIsLoggedin(){
        return isLoggedIn;
    }
    
    public String getUsername(){
        return twv.getUsername();
    }
    
    public void setResetLocation(boolean b){
        resetLocation = b;
    }
    
    public void snapProperties(){
        //System.out.println("-----");
        HashMap<String, String> snapProps = cfw.getProperties();
        if(Boolean.parseBoolean(snapProps.get("RememberUser"))){
            username = getUsername();
        } else {
            username = "";
        }
        snapProps.put("DisableNotifactions", disableNotifications+"");
        snapProps.put("User", username);
        if(resetLocation){
            snapProps.put("PosX", 0+"");
            snapProps.put("PosY", 0+"");
        } else if(Boolean.parseBoolean(snapProps.get("RememberPosition"))){
            snapProps.put("PosX", twv.getLocation().x+"");
            snapProps.put("PosY", twv.getLocation().y+"");
        } else {
            snapProps.put("PosX", "");
            snapProps.put("PosY", "");
        }
        TwitchApplication.getInstance().saveParams(snapProps);
    }
    
    public boolean getNotifications(){
        return disableNotifications;
    }
    
    public void setNotifications(boolean b) {
        this.disableNotifications = b;
    }

    public void trayNotify(MessageState ms, String message) {
        if(disableNotifications) return;
        if(twv.getTray() == null) return;
        try {
            switch(ms){
                default:
                case INFO:
                    twv.trayNotify(trayName, message, TrayIcon.MessageType.INFO);
                    break;
                case ERROR:
                    twv.trayNotify(trayName, message, TrayIcon.MessageType.ERROR);
                    break;
                case WARNING:
                    twv.trayNotify(message, message, TrayIcon.MessageType.WARNING);
            }
        } catch (NullPointerException ex) {
            twv.showMessage(MessageState.WARNING, "Cannot display tray notification");
        }
    }
    
    public ConfigWindow getConfigWindow(){
        return cfw;
    }
    
    public void setLoginButton(boolean b){
        twv.enableButton(b);
    }

    public final void resetLocation() {
        //FIXME: Should determine, if running two screens if they are 2x 1080
        // else, make sure screen is 1080 AND THEN set the location
        cfw.setVisible(false);
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gd = ge.getScreenDevices();
        MonitorUtility.getInstance().windowLocation();
        if (gd.length > 1) {
            setLocation((undecoratedWindow ? -585 : -605), (undecoratedWindow ? 680 : 630));
        } else if (gd.length >= 1) {
            setLocation((undecoratedWindow ? 1405 : 1315), (undecoratedWindow ? 815 : 775));
        } else {
            twv.showMessage(MessageState.ERROR, "Unable to detect screens, cannot set window location.");
        }
    }
    
    public void setLocation(int x, int y){
        twv.setLocation(x, y);
    }
    
    public int[] getLocation(){
        int[] pos = new int[2];
        pos[0] = twv.getLocation().x;
        pos[1] = twv.getLocation().y;
        return pos;
    }
    
    public void setTrayTooltip(String tooltip){
        twv.setTrayTooltip(tooltip);
    }
    
    public boolean isConfigVisible(){
        return cfw.isVisible();
    }
    
    public void showConfigWindow(boolean b) {
        int x = (GraphicsEnvironment.getLocalGraphicsEnvironment().
                getScreenDevices().length > 1) ? 183 : -20; // screen bounds
        int i = (twv.getBounds().y > x) ? 183 : -224; // window offset
        cfw.setLocation(twv.getLocation().x, twv.getLocation().y-i);
        twv.setConfigIcon(b);
        cfw.setVisible(b);
    }
    
    public int getScreenNumber(Window wind){
        Window myWind = wind;
        GraphicsConfiguration config = myWind.getGraphicsConfiguration();
        GraphicsDevice dev = config.getDevice();
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screens = env.getScreenDevices();
        int screen = 0;
        for(int i = 0; i < screens.length; i++){
            if(screens[i].equals(dev)){
                screen = i;
            }
        }
        return screen;
    }
}
