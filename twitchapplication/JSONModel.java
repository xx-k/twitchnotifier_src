package twitchapplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Toby
 */
public class JSONModel {

    private TwitchController twc;

    public JSONModel(TwitchController twc) {
        this.twc = twc;
    }

    private String readUrl(String urlString) {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            URLConnection ucn = url.openConnection();
            ucn.setRequestProperty("Accept", twc.getTwitchAPIVersion());
            ucn.setRequestProperty("Client-ID", twc.getClientID());
            reader = new BufferedReader(new InputStreamReader(ucn.getInputStream()));
            StringBuilder builder = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1) {
                builder.append(chars, 0, read);
            }

            return builder.toString();
        } catch (Exception ex) {
            twc.showMessage(TwitchController.MessageState.WARNING, ex.getClass().getName() + ": Unable to open Twitch TV!");
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    twc.showMessage(TwitchController.MessageState.WARNING, ex.getClass().getName() + ": could not close HTTP stream!");
                }
            }
        }
    }

    /**
     * Retrieve a list of followed channels from a user from the Twitch API
     *
     * @param user The username to get followers from.
     * @return An arraylist containing the followed users.
     */
    public ArrayList<String> getFollowers(String user) {
        String jsonstring = "";
         JSONObject jsonobj = null;
         JSONArray follows = null;
        try {
            jsonstring = readUrl("https://api.twitch.tv/kraken/users/" + user + "/follows/channels");
            jsonobj = new JSONObject(jsonstring);
            
            int channelsInObject = jsonobj.getInt("_total");
            if(channelsInObject > 24){ //Without a specified range the API will respond with a default number of 25 channels. Some users might have more than 25 followed channels.
                if(channelsInObject>99){ // https://github.com/justintv/Twitch-API/blob/master/v3_resources/streams.md - "Maximum number of objects in array. Default is 25. Maximum is 100."
                    twc.showMessage(TwitchController.MessageState.ERROR, "Current version does not support more than 100 followed channels.");  //FIX THIS: need of a better API implementation (using "next" under "_links" to build a larger list).
                    channelsInObject=100;
                } 
                jsonstring = readUrl("https://api.twitch.tv/kraken/users/" + user + "/follows/channels?limit="+channelsInObject);
                 jsonobj = new JSONObject(jsonstring);
            }
            follows = jsonobj.getJSONArray("follows");
            ArrayList<String> al = new ArrayList<>();
            for (int i = 0; i < follows.length(); i++) {
                JSONObject streamer = follows.getJSONObject(i);
                JSONObject channel = streamer.getJSONObject("channel");
                al.add((String) channel.get("name"));
            }
            return al;
        } catch (Exception ex) {
            System.out.println("getFollowers threw");
            ex.printStackTrace();
            System.out.println("--User was: " + user);
            System.out.println("JSON String was: " + jsonstring);
            if(jsonobj == null)
                System.out.println("JSON object was null");
            if(follows == null)
                System.out.println("JSON Array was null");
            else if (ex.getClass().getName().contains("NullPointerException")) {
                //FIXME: "Username not found" has to be determined by some other method - what does the API return if no username was found?
               // twc.showMessage(TwitchController.MessageState.ERROR, "Username not found!");
            } else {
                twc.showMessage(TwitchController.MessageState.WARNING, ex.getClass().getName() + " was thrown");
            }
            twc.setLoginButton(true);
        }
        return null;
    }

    /**
     * Takes a list of users and generates a new arraylist with those that are
     * online.
     *
     * @param list The list of users to be checked.
     * @return Returns a filtered list (online people only).
     */
    private int n = 3; // channel info counter
    public ArrayList<Streamer> getOnline(ArrayList<String> list) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i != list.size() - 1) {
                sb.append(",");
            }
        }
        try {
            String jsonString = readUrl("https://api.twitch.tv/kraken/streams?channel=" + sb);
            JSONObject jbo = new JSONObject(jsonString);
            JSONArray array = jbo.getJSONArray("streams");
            int channelsInObject = array.length();
            if(channelsInObject > 24){ //Without a specified range the API will respond with a default number of 25 channels. Some users might have more than 25 followed channels.
                if(channelsInObject>99){
                    twc.showMessage(TwitchController.MessageState.ERROR, "Current version only supports up to 100 online channels.");
                } 
                jsonString = readUrl("https://api.twitch.tv/kraken/streams?channel=" + sb + "?limit="+channelsInObject);
                 jbo = new JSONObject(jsonString);
                 array = jbo.getJSONArray("streams");
            }
            
            ArrayList<Streamer> al = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject streamer = array.getJSONObject(i);
                JSONObject channel = streamer.getJSONObject("channel");
                String streamerName = (String) channel.get("display_name");
                int viewerCount = streamer.getInt("viewers");
                String streamTitle = (String) channel.get("status");
                String gameTitle = (String) streamer.get("game");
                Streamer addStreamer = new Streamer(streamerName, true, gameTitle, streamTitle, viewerCount);
                al.add(addStreamer);
            }
            return al;
        } catch (Exception ex) {
            ex.printStackTrace();
            twc.showMessage(TwitchController.MessageState.WARNING, ex.getClass().getName() + ": Could not generate channel information!");
            twc.trayNotify(TwitchController.MessageState.ERROR, "Channel information could not be generated!");
            return null;
        }
    }
}
