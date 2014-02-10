package twitchapplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;


public class UpdateUtility  {
    
    private static UpdateUtility singleton;
    
    public static UpdateUtility getInstance() {
        if(singleton == null) {
            singleton = new UpdateUtility();
        }
        return singleton;
    }
    
    
    
    public enum UpdateStatus {
        NO_UPDATE,
        NEW_UPDATE,
        INVALID_RESPONSE,
        TIMEOUT_UPDATE
    }
    
    private final String updateUrl = "https://github.com/xx-k/twitchnotifier_public";
    
    //private final String serverUrl = "localhost";
    private final String serverUrl = "87.60.137.185";
    private final String requestPath = "/request-version";
    private final int serverPort = 8000;
    
    private TwitchController ctr;
    
    private String currentVersion; // store the current version
    
    private UpdateStatus currentStatus = UpdateStatus.TIMEOUT_UPDATE;
    private Timer timeoutTimer;
    private int timeoutTime = 3 * 1000; // 3000 ms timeout
    
    public UpdateUtility(){
        initTimers();
    }
    
    public UpdateUtility setController(TwitchController ctr) {
        this.ctr = ctr;
        return this;
    }
    
    public UpdateUtility setCurrentVersion(String currVers) {
        this.currentVersion = currVers;
        return this;
    }
    
    public String getUpdateUrl(){
        return updateUrl;
    }
    
    private void initTimers() {
        if(timeoutTimer == null) {
            timeoutTimer = new Timer();
        }
        timeoutTimer.schedule(new TimerTask(){
            @Override
            public void run() {
                if(currentStatus == UpdateStatus.TIMEOUT_UPDATE) {
                    updateVersion(null);
                }
            }
        }, timeoutTime);
    }

    public void updateVersion(String[] v) {
        if (v != null) {
            if(!v[0].contains("200")) {
                currentStatus = UpdateStatus.INVALID_RESPONSE;
            } else {
                currentStatus = versionComparison(currentVersion, v[1]);
            }
        }
        ctr.getConfigWindow().setUpdate(currentStatus);
    }
    
    
    // might not be the best code, because 
   //if the current version is greater than new version, then a "new update" will still be triggered
    private UpdateStatus versionComparison(String newVersion, String oldVersion) {
        if(newVersion.length() != oldVersion.length()){
            throw new IllegalArgumentException("Versions must have equal length");
        }
        char[] newArray = newVersion.toCharArray();
        char[] oldArray = oldVersion.toCharArray();
        for(int i = 0; i < newArray.length; i++) {
            if(newArray[i] == '.') continue;
            if(newArray[i] > oldArray[i]){
                return UpdateStatus.NEW_UPDATE;
            }
        }
        return UpdateStatus.NO_UPDATE;
    }
    
    
    private void checkVersion() {
            if (ctr == null || currentVersion.isEmpty() || currentVersion == null) {
                throw new IllegalStateException("Invalid state");
            }
         BufferedReader reader = null;
        String notFound = "404 Not found";
        try {
            URL url = new URL("http", serverUrl, serverPort, requestPath);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (conn.getResponseCode() == 404) {
                currentStatus = UpdateStatus.INVALID_RESPONSE;
            }
            InputStreamReader isr = new InputStreamReader(conn.getInputStream(), "UTF-8");
            reader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            conn.disconnect();
            String[] strArray = new String[2];
            strArray[0] = ""+conn.getResponseCode();
            strArray[1] = sb.toString();
            updateVersion(strArray);
        } catch (Exception ex) {
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ex) {}
            }
        }
    }
    
    public void startCheck() {
        Thread t = new Thread(new Runnable(){
            @Override
            public void run() {
                checkVersion();
            }
        });
        t.run();
    }

    
}
