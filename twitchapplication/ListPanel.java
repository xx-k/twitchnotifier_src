package twitchapplication;

import java.awt.*;
import java.util.*;
import java.util.Timer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ListPanel extends javax.swing.JPanel {

    private TwitchController twc;
    private Timer timer;
    
    private int counter = 0; //used to determine if we should notify
    private int timeout = 30;
    
    private boolean popoutVideo;

    private ArrayList<String> internalTracker = new ArrayList<>();
    private ArrayList<String> internalOffline;
    private ArrayList<Streamer> internalOnline;
    
    private ArrayList<Streamer> internalCopy;
    
    
    //Store streamer going online, to be used when opening stream from tray
    private String recentOnline = "";
       
    // Arrows in list
    private final ImageIcon downIcon = new ImageIcon(getClass().getResource("/res/down.png"));
    private final ImageIcon upIcon = new ImageIcon(getClass().getResource("/res/up.png"));
  
    
    /**
     * Creates new form ListPanel
     *
     * @param twc A twitch controller reference
     */
    public ListPanel(final TwitchController twc) {
        this.twc = twc;
        initComponents();
        
        onlineScrollPane.setHorizontalScrollBar(null);
        offlineScrollPane.setHorizontalScrollBar(null);   
    }

    /**
     * Populate the "online"/"offline" panels
     *
     * @param list An arraylist with the streamers
     */
    public void generateLists(ArrayList<Streamer> list) {
        internalOnline = new ArrayList<>();
        internalOffline = new ArrayList<>();
        internalCopy = list;
        int i = 0;
        for(i = 0; i < list.size(); i++){
            if(list.get(i).isStatus()){
                internalOnline.add(list.get(i)); // add to online list
                if(!internalTracker.contains(list.get(i).getStreamerName())){ // if this online user wasn't already on the tracker
                    internalTracker.add(list.get(i).getStreamerName()); // add to tracker
                    if(counter>0){ // do not notify user first time!
                        String message = list.get(i).getStreamerName() +" just went online!";
                        recentOnline = list.get(i).getStreamerName();
                        twc.showMessage(TwitchController.MessageState.INFO, message);
                        twc.trayNotify(TwitchController.MessageState.INFO, message);
                    }
                }
            } else {
                internalOffline.add(list.get(i).getStreamerName());
            }
        }
        if(counter==0){
            twc.showOnline(internalOnline);
        }
        Iterator it = internalTracker.iterator();
        ArrayList<String> internalOnlineTrack = new ArrayList<>();
        for(Streamer strx : internalOnline){
            internalOnlineTrack.add(strx.getStreamerName());
        }
        while(it.hasNext()){
            String str = (String)it.next();
            if(!internalOnlineTrack.contains(str)){
                it.remove();
                twc.showMessage(TwitchController.MessageState.INFO, str + " went offline.");
                twc.trayNotify(TwitchController.MessageState.INFO, str + " went offline.");
                recentOnline = "";
            }
            
        }
        if (timer == null) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if(pauseSelect.isSelected()){
                        return;
                    }
                    class MyWorker extends SwingWorker<String, Object> {

                        @Override
                        protected String doInBackground() {
                            progressBar.setIndeterminate(true);
                            twc.showMessage(TwitchController.MessageState.BLANK, "");
                            twc.update();
                            System.gc();
                            return "Done.";
                        }

                        @Override
                        protected void done() {
                            progressBar.setIndeterminate(false);
                        }
                    }
                    new MyWorker().execute();
                }
            }, timeout * 1000, timeout * 1000);
        }
        
        drawList(list);
        
        
        twc.setTrayTooltip(internalOnline.size() + " streamers online");
        counter++;
    }

    /*
     * Stop the timer and cancel future tasks.
     */
    public void timerStop() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }
    
    private void drawList(ArrayList<Streamer> list){
        onlineContentPane.removeAll();
        offlineContentPane.removeAll();
        
        GridLayout onlineLayout = new GridLayout(
                (internalOnline.size() < 4) ? internalOnline.size()+3 : internalOnline.size(), 1); // ternary is because grid messes up (by placing evenly), in a list suited for at least 5 elements ( still needs a bit of fine-tuning)
        GridLayout offlineLayout = new GridLayout(internalOffline.size(), 1);
        
        onlineContentPane.setLayout(onlineLayout);
        offlineContentPane.setLayout(offlineLayout);

        for(Streamer strX : list) {
            if(strX.isStatus()){
                JLabel urlLabel = generateURLLabel(strX.getDisplayName(), strX.getViewers());
                String gameTitle = (strX.getGameTitle().isEmpty()) ? "" : " @ " + strX.getGameTitle();
                urlLabel.setToolTipText(strX.getStreamTitle()+gameTitle);
                onlineContentPane.add(urlLabel);
            } else {
                JLabel urlLabel = generateURLLabel(strX.getDisplayName(), -1); // -1 to ensure a difference can be made from newly online streamers
                offlineContentPane.add(urlLabel);
            }
        }
        
        offlineContentPane.revalidate();
        offlineContentPane.repaint();
        onlineContentPane.repaint();
        onlineContentPane.revalidate();
    }
    
    public void resetCounter(){
        counter = 0;
    }
    
    public ArrayList<Streamer> getOnline(){
        if(internalOnline != null && !internalOnline.isEmpty()){
            return internalOnline;
        }
        return null;
    }
    
    public String getRecentOnline(){
        return recentOnline;
    }
    
    public void clearRecentOnline(){
        recentOnline = "";
    }
   
    private URLLabel generateJLabel(String text, boolean online) {
        URLLabel genLabel = new URLLabel();
        genLabel.setText(text);
        if (online) {
            genLabel.setIcon(new ImageIcon(getClass().getResource("/res/accept.png")));
        } else {
            genLabel.setIcon(new ImageIcon(getClass().getResource("/res/cross.png")));
        }
        genLabel.setVisible(true);
        return genLabel;
    }
    
    private JLabel generateURLLabel(String streamerName, int viewers) {
        String url = "http://www.twitch.tv/" + streamerName;
        url = url + (popoutVideo ? "/popout" : "");
        URLLabel label;
        if (viewers > -1) {
            label = generateJLabel(streamerName + " (" + viewers + ")", true);
        } else {
            label = generateJLabel(streamerName, false);
        }
        label.setBorder(new EmptyBorder(1,3,1,0));
        label.setURL(url);
        return label;
    }
    
    public void setTimer(int timer){
        this.timeout = timer;
    }


    public void setPopoutVideo(boolean popoutVideo) {
        this.popoutVideo = popoutVideo;
    }
    
    public void redrawList(){
        if(internalOnline != null && internalOffline != null){
            drawList(internalCopy);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        logoutButton = new javax.swing.JButton();
        progressBar = new javax.swing.JProgressBar();
        pauseSelect = new javax.swing.JCheckBox();
        onlineScrollPane = new javax.swing.JScrollPane();
        onlineContentPane = new javax.swing.JPanel();
        offlineScrollPane = new javax.swing.JScrollPane();
        offlineContentPane = new javax.swing.JPanel();

        logoutButton.setText("Logout");
        logoutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logoutButtonActionPerformed(evt);
            }
        });

        pauseSelect.setText("Pause Update");

        onlineScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Online", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 10), new java.awt.Color(0, 0, 0))); // NOI18N

        javax.swing.GroupLayout onlineContentPaneLayout = new javax.swing.GroupLayout(onlineContentPane);
        onlineContentPane.setLayout(onlineContentPaneLayout);
        onlineContentPaneLayout.setHorizontalGroup(
            onlineContentPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 217, Short.MAX_VALUE)
        );
        onlineContentPaneLayout.setVerticalGroup(
            onlineContentPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 113, Short.MAX_VALUE)
        );

        onlineScrollPane.setViewportView(onlineContentPane);

        offlineScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Offline", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 10), new java.awt.Color(0, 0, 0))); // NOI18N

        javax.swing.GroupLayout offlineContentPaneLayout = new javax.swing.GroupLayout(offlineContentPane);
        offlineContentPane.setLayout(offlineContentPaneLayout);
        offlineContentPaneLayout.setHorizontalGroup(
            offlineContentPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 223, Short.MAX_VALUE)
        );
        offlineContentPaneLayout.setVerticalGroup(
            offlineContentPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 113, Short.MAX_VALUE)
        );

        offlineScrollPane.setViewportView(offlineContentPane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(logoutButton)
                .addGap(28, 28, 28)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pauseSelect)
                .addContainerGap(71, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(onlineScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(offlineScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(logoutButton)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pauseSelect))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(onlineScrollPane)
                    .addComponent(offlineScrollPane)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void logoutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoutButtonActionPerformed
        twc.setContentPanel(0);
    }//GEN-LAST:event_logoutButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton logoutButton;
    private javax.swing.JPanel offlineContentPane;
    private javax.swing.JScrollPane offlineScrollPane;
    private javax.swing.JPanel onlineContentPane;
    private javax.swing.JScrollPane onlineScrollPane;
    private javax.swing.JCheckBox pauseSelect;
    private javax.swing.JProgressBar progressBar;
    // End of variables declaration//GEN-END:variables
}
