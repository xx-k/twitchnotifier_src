package twitchapplication;

import java.util.Timer;
import java.util.TimerTask;
import org.vertx.java.core.*;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientResponse;


public class UpdateUtility {
    
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
    private final String serverUrl = "ec2-54-216-201-2.eu-west-1.compute.amazonaws.com";
    private final String requestPath = "/request-version";
    private final int serverPort = 8000;
    
    private TwitchController ctr;
    
    private String currentVersion; // store the current version
    
    private UpdateStatus currentStatus = UpdateStatus.TIMEOUT_UPDATE;
    private Timer timeoutTimer;
    private int timeoutTime = 3 * 1000; // 3000 ms timeout
    
    private final Vertx vertx = VertxFactory.newVertx();
    
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
    
    
    public void checkVersion() {
        if (ctr == null || currentVersion.isEmpty() || currentVersion == null) {
            throw new IllegalStateException("Invalid state");
        }
        HttpClient httpClient = vertx.createHttpClient();
        httpClient.exceptionHandler(new Handler() {
                        @Override
                        public void handle(Object e) {} // just ignore whatever exceptions that are thrown, they're not important right now
                    })
                  .setHost(serverUrl)
                  .setPort(serverPort)
                  .getNow(requestPath, new Handler<HttpClientResponse>() {
                    @Override
                    public void handle(final HttpClientResponse res) {
                        res.bodyHandler(new Handler<Buffer>() {
                            @Override
                            public void handle(Buffer buf) {
                                String[] versInfo = new String[2];
                                StringBuilder sb = new StringBuilder(Integer.toString(res.statusCode())).append(" ").append(res.statusMessage());
                                versInfo[0] = sb.toString();
                                versInfo[1] = buf.toString();
                                updateVersion(versInfo);
                            }
                        });
                    }
                });
    }

    
}
