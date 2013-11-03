package twitchapplication;

import java.io.*;
import java.util.*;

/**
 *
 * @author Toby
 */
public class TwitchApplication {

    private final String appVersion = "0.7.4";
    
    private static TwitchApplication twa;

    public static TwitchApplication getInstance() {
        if (twa == null) {
            twa = new TwitchApplication();
        }
        return twa;
    }
    
    private final String configFilename = System.getProperty("user.home")+System.getProperty("file.separator")+"twitch.conf";
    
    
    private String username = "";
    private boolean disableNotifications = true;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//      This part is used to test the headers of the http request.
//      Requires "readURL" to be public.        
//        if(args[0].equalsIgnoreCase("testurl")){
//            TwitchController twc = new TwitchController(false);
//            System.out.println(twc.readUrl("http://localhost:8080/HeaderCheckerApplication/HeaderChecker"));  
//            System.exit(0);
//        }
        try {
            Runtime.getRuntime().addShutdownHook(new SaveProperties());
        } catch (Exception ex){
            ex.printStackTrace();
            System.exit(-1);
        }
            TwitchApplication twiapp = getInstance();
    }

    public boolean deleteParams() {
        File f = null;

        try {
            f = new File(configFilename);
            return f.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    
    private static class SaveProperties extends Thread {
        @Override
        public void run(){
            TwitchApplication.getInstance().twc.snapProperties();
        }
    }
    
    
    private TwitchController twc;

    private HashMap<String, String> loadParams() {
        Properties props = new Properties();
        InputStream is = null;

        try {
            File f = new File(System.getProperty("user.home")+System.getProperty("file.separator")+"twitch.conf");
            is = new FileInputStream(f);
        } catch (Exception e) {
            is = null;
        }

        try {
            if (is == null) {
                is = getClass().getResourceAsStream(System.getProperty("user.home")+System.getProperty("file.separator")+"twitch.conf");
            }
            props.load(is);
        } catch (Exception e) {}

        HashMap<String, String> loadParams = new HashMap<>();
        String[][] properties = PropertiesManager.getProperties();
        for(int i = 0; i < properties.length; i++){
           loadParams.put(properties[i][0], props.getProperty(properties[i][0], properties[i][1]));
        }
        System.out.println("Loading params");
        return loadParams;
    }

    private TwitchApplication() {
        twc = new TwitchController();
        twc.loadParams(loadParams());
        twc.showMessage(TwitchController.MessageType.INFO, "Current version: " +appVersion);
    }

    public void saveParams(HashMap<String, String> paramsMap) {
        try {
            Properties props = new Properties();
            String[][] properties = PropertiesManager.getProperties();
            System.out.println("Saving params.");
            for(int i = 0; i < properties.length; i++){
                props.setProperty(properties[i][0], paramsMap.get(properties[i][0]));
            }
            if(paramsMap.get("PosX")!=null&&paramsMap.get("PosY")!=null){
                props.setProperty("PosX", paramsMap.get("PosX"));
                props.setProperty("PosY", paramsMap.get("PosY"));
            } else {
                props.setProperty("PosX", "");
                props.setProperty("PosY", "");
            }
            File f = new File(System.getProperty("user.home")+System.getProperty("file.separator")+"twitch.conf");
            OutputStream out = new FileOutputStream(f);
            props.store(out, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
