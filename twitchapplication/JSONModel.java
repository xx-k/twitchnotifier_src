package twitchapplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

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
    
    private final String twitchAPIversion;
    private final String twitchClientID;

    public JSONModel(TwitchController twc) {
        this.twc = twc;
        res = result.WAITING;
        this.twitchAPIversion = twc.getTwitchAPIVersion();
        this.twitchClientID = twc.getClientID();
    }

    private String readUrl(String urlString) {
        BufferedReader reader = null;
        String notFound = "{\"error\":\"Not Found\",\"status\":404,\"message\":\"User does not exist\"}";
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Accept", twitchAPIversion);
            conn.setRequestProperty("Client-ID", twitchClientID);
            if (conn.getResponseCode() == 404) {
                res = result.USERNAME_NOT_FOUND;
                return notFound;
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

    public ArrayList<Streamer> generateList(String username) {
        res = result.INTERNAL_ERROR;
        if (username == null) {
            throw new IllegalArgumentException("Username was null.");
        }

        res = result.WAITING;

        String followedURL =
                baseURL + "/users/" + username + "/follows/channels?limit=" + 100;
        String nextFollowedURL;
        String followedJSONString = "";
        JSONObject followedJSONObject;
        JSONArray followedJSONArray;

        String streamersURL = "";
        String streamersJSONString;
        JSONObject streamersJSONObject;
        JSONArray streamersJSONArray;
        
        boolean finished = false;
        int totalChannels = 0;

        ArrayList<Streamer> newResultList = new ArrayList<>();
        try {

            while(!finished) {
                followedJSONString = readUrl(followedURL);
                followedJSONObject = new JSONObject(followedJSONString);
                if (followedJSONObject.has("error")) {
                    res = result.USERNAME_NOT_FOUND;
                    throw new IOException();
                }
                followedJSONArray = followedJSONObject.getJSONArray("follows");
                totalChannels = followedJSONObject.getInt("_total");
                nextFollowedURL = followedJSONObject.getJSONObject("_links").getString("next");
                if(followedJSONArray.length()==0) {
                    break; // the "offset" doesn't really care if there isn't any streams in next reqest, so break loop if we got an empty streams[]
                           // TODO: Might need to do some offset calculations to reduce amount of API requests
                }

                //First we generate a list of streamers, all offline
                for (int i = 0; i < followedJSONArray.length(); i++) {
                    JSONObject streamer = followedJSONArray.getJSONObject(i);
                    JSONObject channel = streamer.getJSONObject("channel");

                    String channelName = channel.getString("name");
                    String displayName = channel.getString("display_name");
                    String gameTitle = "";
                    String streamTitle = "";
                    if (channel.get("game") != org.json.JSONObject.NULL) {
                        gameTitle = channel.getString("game");
                    }
                    if (channel.get("status") != org.json.JSONObject.NULL) {
                        streamTitle = channel.getString("status");
                    }

                    Streamer addStreamer = new Streamer(displayName, channelName,
                            false, gameTitle, streamTitle, -1); // -1 to ensure a difference can be made from newly online streamers
                    newResultList.add(addStreamer);
                }

                StringBuilder streamersQuery = new StringBuilder();
                for (int i = 0; i < newResultList.size(); i++) {
                    String appendName = newResultList.get(i).getChannelName();
                    streamersQuery.append(appendName);
                    if (i != newResultList.size() - 1) {
                        streamersQuery.append(",");
                    }
                }


                streamersURL = baseURL + "/streams?channel=" + streamersQuery + "&limit=" + 100;
                streamersJSONString = readUrl(streamersURL);
                streamersJSONObject = new JSONObject(streamersJSONString);
                streamersJSONArray = streamersJSONObject.getJSONArray("streams");

                // Now we have an array with online channels, time to mark online ones...

                // onlineChannelIndex => j
                // "offline"ChannelIndex => i
                for (int j = 0; j < streamersJSONArray.length(); j++) {
                    JSONObject streamer = streamersJSONArray.getJSONObject(j);
                    int viewers = streamer.getInt("viewers");
                    JSONObject channel = streamer.getJSONObject("channel");
                    String channelName = channel.getString("name");
                    for (int i = 0; i < newResultList.size(); i++) {
                        if (newResultList.get(i).getChannelName().equals(channelName)) {
                            newResultList.get(i).setStatus(true);
                            newResultList.get(i).setViewers(viewers);
                        }
                    }
                }
                if(totalChannels < 100) {
                    finished = true;
                } else {
                    twc.showMessage(TwitchController.MessageState.INFO, "Building list... (Above 100 followers, please wait!)");
                    followedURL = nextFollowedURL;
                }
            }
            return newResultList;
        } catch (Exception ex) {
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
                    System.out.println("jsonString was:" + followedJSONString);
                    ex.printStackTrace();
                    break;
            }
            twc.setLoginButton(true);
            return null;
        }
    }
}
