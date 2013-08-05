/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package twitchapplication;

import java.awt.*;
import java.util.*;
import javax.swing.*;

/**
 *
 * @author Toby
 */
public class TwitchController {

    private final String twitchAPIVersion = "application/vnd.twitchtv.v2+json";
    private final String clientID = "q4dpzyshmhid2kjx5u0n6s34odf533k";
    private final String trayName = "Twitch Notifier";
    
    
    private TwitchView twv;
    private ConfigWindow cfw;
    private JSONModel jsm;
    
    private boolean isLoggedIn;
    
    
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
    
    public TwitchController(boolean b) {
        jsm = new JSONModel(this);
    }

    public String getTwitchAPIVersion() {
        return twitchAPIVersion;
    }

    public String getClientID() {
        return clientID;
    }
    
    
    public void showMessage(int i, String msg) {
        twv.showMessage(i, msg);
    }
    
    public ArrayList<Streamer> generateOnlineList(String username) {
        ArrayList<String> follows = jsm.getFollowers(username);
        //ArrayList<String> onlineList = jsm.getOnline(follows);
        ArrayList<Streamer> onlineList = jsm.getOnline(follows);
        ArrayList<Streamer> streamers = new ArrayList<>();
        ArrayList<String> alreadyAdded = new ArrayList<>();
        int i;
        int j;
        for (i = 0; i < follows.size(); i++) { // i is index at "follows" list
            if (!alreadyAdded.contains(follows.get(i))) {
                for (j = 0; j < onlineList.size(); j++) { // j is index at "online" list
                    if (follows.get(i).equalsIgnoreCase(onlineList.get(j).getStreamerName())) {
                        //streamers.add(new Streamer(follows.get(i), true));
                        streamers.add(onlineList.get(j));
                        alreadyAdded.add(follows.get(i));
                    }
                }
                if(!alreadyAdded.contains(follows.get(i))){
                    streamers.add(new Streamer(follows.get(i), false));
                }
                alreadyAdded.add(follows.get(i));
            }
        }
        return streamers;
    }

    public void fireUsername(String text) {
        twv.hideLabel();
        this.username = text;
        twv.enableButton(false);
        ArrayList<Streamer> streamers = generateOnlineList(username);
        if (streamers != null) {
            twv.setContentPanel(1);
            twv.generateContent(streamers);
        }
    }
    
    public void setUsername(String un) {
        twv.setUsername(un);
    }

    public void update() {
        fireUsername(username);
    }

    public void setContentPanel(int i) {
        twv.setContentPanel(i);
    }
    
    public void toggleWindow(boolean b){
        twv.setVisible(b);
        showConfigWindow(false);
        if(!b) twv.configButtonCounter = 0;
        twv.setConfigIcon(false);
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
        
        int[] x = null;
        if(Boolean.parseBoolean(params.get("RememberPosition"))){
            x = new int[2];
            try{
            x[0] = Integer.parseInt(params.get("PosX"));
            x[1] = Integer.parseInt(params.get("PosY"));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(twv, "Could not save position!");
                x[0] = 0;
                x[1] = 0;
            }
        }
        if(i++ == 0){
            startGui(x, params);
        } else {
            twv.setPopoutVideo(popoutVideos);
        }
    }
  
    private void startGui(int[] i, HashMap<String, String> map) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            twv.showMessage(3, ex.getClass().getName() + ": Could not set layout, using default UI");
        }
        JFrame.setDefaultLookAndFeelDecorated(true);
        twv = new TwitchView(this, undecoratedWindow);
        cfw = new ConfigWindow(this);
        if(i!=null){
            twv.setLocation(i[0], i[1]);
        } else {
            resetLocation(0);
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
        if (startInTray) {
            StringBuilder sb;
            if (onlineStreamers == null) {
                sb = new StringBuilder("No streamers online.");
            } else {
                sb = new StringBuilder("Following streamers are online:\n");
                for (Streamer str : onlineStreamers) {
                    sb.append(str.getStreamerName()).append(" (" + str.getViewers()+ ")").append("\n");
                }
            }
            trayNotify(sb.toString());
        }
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
    
    public String getUsername(){
        return twv.getUsername();
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

    public void trayNotify(String message) {
        if(disableNotifications) return;
        if(twv.getTray() == null) return;
        try {
            twv.getTray().displayMessage(trayName, message, TrayIcon.MessageType.INFO);
        } catch (NullPointerException ex) {
            twv.showMessage(0, "Cannot display tray notification");
        }
    }
    
    public ConfigWindow getConfigWindow(){
        return cfw;
    }
    
    public void setLoginButton(boolean b){
        twv.enableButton(b);
    }

    private int startAtScreen = 0;
    public final void resetLocation(int p) {
        cfw.setVisible(false);
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gd = ge.getScreenDevices();
        if(p == 1){
            resetLocation = true;
        }
        if (gd.length > 1) {
            startAtScreen = 1;
            setLocation((undecoratedWindow ? -585 : -605), (undecoratedWindow ? 680 : 630));
        } else if (gd.length >= 1) {
            setLocation((undecoratedWindow ? 1405 : 1315), (undecoratedWindow ? 815 : 775));
        } else {
            twv.showMessage(0, "Unable to detect screens, cannot set window location.");
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
    
    public void setTrayTooltip(String tooltip){
        twv.setTrayTooltip(tooltip);
    }

    public void showConfigWindow(boolean b) {
        int x = (GraphicsEnvironment.getLocalGraphicsEnvironment().
                getScreenDevices().length > 1) ? 183 : -20; // screen bounds
        int i = (twv.getBounds().y > x) ? 183 : -224; // window offset
        cfw.setLocation(twv.getLocation().x, twv.getLocation().y-i);
        twv.setConfigIcon(b);
        cfw.setVisible(b);
    }
}
