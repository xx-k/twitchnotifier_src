/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package twitchapplication;

/**
 *
 * @author Toby
 */
public class Streamer {
    private boolean status = false;
    private String channelName = "";
    private String displayName = "";
    private int viewers = 0;
    private String streamTitle = "";
    private String gameTitle = "";
    

    public Streamer(String displayName, String channelName, boolean status, String gameTitle, String streamTitle, int viewers){
        this.viewers = viewers;
        this.status = status;
        this.gameTitle = gameTitle;
        this.streamTitle = streamTitle;
        this.displayName = displayName;
        this.channelName = channelName;
    }
    
    /**
     * Get the online/offline status of the streamer.
     * @return 
     */
    public boolean isStatus() {
        return status;
    }

    public int getViewers() {
        return viewers;
    }

    public void setViewers(int viewers) {
        this.viewers = viewers;
    }
    
    public String getChannelName(){
        return channelName;
    }
    
    public String getDisplayName(){
        return displayName;
    }
    
    
    /**
     * Set the online/offline status
     * @param status the new status
     */
    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getStreamerName() {
        return displayName;
    }

    public void setStreamerName(String streamerName) {
        this.displayName = streamerName;
    }

    public String getStreamTitle() {
        return streamTitle;
    }

    public void setStreamTitle(String streamTitle) {
        this.streamTitle = streamTitle;
    }

    public String getGameTitle() {
        return gameTitle;
    }

    public void setGameTitle(String gameTitle) {
        this.gameTitle = gameTitle;
    }
    
    @Override
    public String toString(){
        return "Streamer " + displayName + "@" + Integer.toHexString(hashCode());
    }
}
