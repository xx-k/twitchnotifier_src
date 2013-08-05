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
    private String streamerName = "";
    private int viewers = 0;
    
    /**
     * Create a Streamer wrapper object
     * @param name The name of the streamer
     * @param status Online/Offline status
     */
    public Streamer(String name, boolean status){
        this.status = status;
        this.streamerName = name;
        this.viewers = 0;
    }
    
    public Streamer(String name, boolean status, int viewers){
        this.status = status;
        this.streamerName = name;
        this.viewers = viewers;
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
    
    
    
    /**
     * Set the online/offline status
     * @param status the new status
     */
    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getStreamerName() {
        return streamerName;
    }

    public void setStreamerName(String streamerName) {
        this.streamerName = streamerName;
    }
    
    @Override
    public String toString(){
        return "Streamer " + streamerName + "@" + Integer.toHexString(hashCode());
    }
}
