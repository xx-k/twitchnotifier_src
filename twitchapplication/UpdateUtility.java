package twitchapplication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateUtility {
    
    
    private static UpdateUtility singleton;
    
    public static UpdateUtility getInstance() {
        if(singleton == null) {
            singleton = new UpdateUtility();
        }
        return singleton;
    } 
    
    
    private final String serverURL = "";
    
    public UpdateUtility(){}
    
    public boolean getUpdateAvailable(String oldVersion) {
        String newVersion = "";
        try {
            URL updateServer = new URL(serverURL);
            HttpURLConnection conn = (HttpURLConnection) updateServer.openConnection();
            conn.setRequestProperty("version-status", "request");
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String version;
            while ((version = reader.readLine()) != null){
                newVersion = version;
            }
            reader.close();
            if(!newVersion.equals(oldVersion)) { //FIXME: improve logic for checking for new versions
                System.out.println("version difference out!");
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        System.out.println("no new version");
        return false;
    }
    
    
}
