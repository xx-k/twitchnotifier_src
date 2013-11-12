package twitchapplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Toby
 */
public class JSONModel {

    private enum request {

        READ_FOLLOWERS, READ_ONLINE;
    }

    private enum result {
        WAITING,
        USERNAME_NOT_FOUND,
        TWITCH_OFFLINE,
        INTERNAL_ERROR,
        CHANNEL_OVERFLOW,
        SUCESSFUL;
    }
    
    private TwitchController twc;
    private result res;
    private final String baseURL = "https://api.twitch.tv/kraken";
    
    
    public JSONModel(TwitchController twc) {
        this.twc = twc;
        res = result.WAITING;
    }

    private String readUrl(String urlString) {
        BufferedReader reader = null;
        String notFound = "{\"error\":\"Not Found\",\"status\":404,\"message\":\"User does not exist\"}";
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Accept", twc.getTwitchAPIVersion());
            conn.setRequestProperty("Client-ID", twc.getClientID());
            if (conn.getResponseCode() == 404) {
                res = result.USERNAME_NOT_FOUND;
                return notFound;
            }
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            conn.disconnect();
            return sb.toString();
        } catch (Exception ex) {
            res = result.TWITCH_OFFLINE;
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

    private List requestUserInfo(request req, String username, List<String> followedChannels) {
        res = result.INTERNAL_ERROR;
        if (req == request.READ_FOLLOWERS && followedChannels != null) {
            throw new IllegalArgumentException("Cannot request followers with followed channels already being given.");
        } else if (req == request.READ_ONLINE && followedChannels == null) {
            throw new IllegalArgumentException("Cannot request online channels without followed channels being given.");
        } else if (!username.isEmpty() && followedChannels != null) {
            throw new IllegalArgumentException("Cannot accept username if followed channels has been given.");
        }

        res = result.WAITING;

        String jsonString = "";
        JSONObject jsonObj = null;
        JSONArray array = null;
        StringBuilder sb = null;
        int totalElements = 0;
        List resultList = null;
        try {
            sb = new StringBuilder();
            switch (req) {
                case READ_FOLLOWERS:
                    jsonString = readUrl(baseURL+"/users/" + username + "/follows/channels");
                    break;
                case READ_ONLINE:
                    for (int i = 0; i < followedChannels.size(); i++) {
                        sb.append(followedChannels.get(i));
                        if (i != followedChannels.size() - 1) {
                            sb.append(",");
                        }
                    }
                    jsonString = readUrl(baseURL+"/streams?channel=" + sb);
                    break;
            }
            jsonObj = new JSONObject(jsonString);
            if (jsonObj.has("error")) {
                res = result.USERNAME_NOT_FOUND;
                throw new IOException();
            }
            switch (req) {
                case READ_FOLLOWERS:
                    totalElements = jsonObj.getInt("_total");
                    break;
                case READ_ONLINE:
                    array = jsonObj.getJSONArray("streams");
                    totalElements = array.length();
                    break;
            }
            if (totalElements > 24) { //Without a specified range the API will respond with a default number of 25 channels. Some users might have more than 25 followed channels.
                if (totalElements > 99) {
                    res = result.CHANNEL_OVERFLOW;  // FIXME ...
                    throw new Exception();
                }
                switch (req) {
                    case READ_FOLLOWERS:
                        jsonString = readUrl(baseURL+"/users/" + username + "/follows/channels?limit=" + totalElements);
                        jsonObj = new JSONObject(jsonString);
                        array = jsonObj.getJSONArray("follows");
                        break;

                    case READ_ONLINE:
                        jsonString = readUrl(baseURL+"/streams?channel=" + sb + "?limit=" + totalElements);
                        jsonObj = new JSONObject(jsonString);
                        array = jsonObj.getJSONArray("streams");
                        break;
                }
            }
            resultList = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject streamer = array.getJSONObject(i);
                JSONObject channel = streamer.getJSONObject("channel");
                if (req == request.READ_ONLINE) {
                    String streamerName = (String) channel.get("display_name");
                    int viewerCount = streamer.getInt("viewers");
                    String streamTitle = (String) channel.get("status");
                    String gameTitle = (String) streamer.get("game");
                    Streamer addStreamer = new Streamer(streamerName, true, gameTitle, streamTitle, viewerCount);
                } else {
                    resultList.add((String) channel.get("name"));
                }
            }
            res = result.SUCESSFUL;
            return resultList;
        } catch (Exception ex) {
            twc.trayNotify(TwitchController.MessageState.ERROR, "An error occurred, please check program!");
            switch (res) {
                case TWITCH_OFFLINE:
                    twc.showMessage(TwitchController.MessageState.WARNING, ex.getClass().getName() + ": Unable to open Twitch TV!");
                    break;

                case USERNAME_NOT_FOUND:
                    twc.showMessage(TwitchController.MessageState.ERROR, "Username was not found!");
                    break;

                case CHANNEL_OVERFLOW:
                    twc.showMessage(TwitchController.MessageState.ERROR, "Current version only supports up to 100 online channels.");
                    break;

                case INTERNAL_ERROR:
                    twc.showMessage(TwitchController.MessageState.WARNING, "Application error, please contact developer!");
                    break;

                default:
                    twc.showMessage(TwitchController.MessageState.WARNING, ex.getClass().getName() + " was thrown!");
                    ex.printStackTrace();
                    break;
            }
            twc.setLoginButton(true);
            return null;
        }
    }

    /**
     * Retrieve a list of followed channels from a user from the Twitch API
     *
     * @param user The username to get followers from.
     * @return An arraylist containing the followed users.
     */
    public List<String> getFollowers(String user) {
        return requestUserInfo(request.READ_FOLLOWERS, user, null);
    }

    /**
     * Takes a list of users and generates a new list with those that are
     * online.
     *
     * @param list The list of users to be checked.
     * @return Returns a filtered list (online people only).
     */
    public List<Streamer> getOnline(List<String> list) {
        return requestUserInfo(request.READ_ONLINE, "", list);
    }
}
