package twitchapplication;


import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.JOptionPane;


/**
 *
 * @author Toby
 */
public class TwitchView extends javax.swing.JFrame {
    
    public int configButtonCounter = 0;
    private TwitchController twc;
    private TrayIcon trayIcon;
    
    // Define icons
    private final javax.swing.ImageIcon errorIcon = new javax.swing.ImageIcon(getClass().getResource("/res/error.png"));
    private final javax.swing.ImageIcon infoIcon = new javax.swing.ImageIcon(getClass().getResource("/res/info.png"));
    private final javax.swing.ImageIcon warningIcon = new javax.swing.ImageIcon(getClass().getResource("/res/warning.png"));
    private final javax.swing.ImageIcon windowIcon = new javax.swing.ImageIcon(getClass().getResource("/res/icon.png"));
    private final javax.swing.ImageIcon configOpenIcon = new javax.swing.ImageIcon(getClass().getResource("/res/config_open.png"));
    private final javax.swing.ImageIcon configCloseIcon = new javax.swing.ImageIcon(getClass().getResource("/res/config_close.png"));
    private final javax.swing.ImageIcon trayOn = new javax.swing.ImageIcon(getClass().getResource("/res/tray_on.png"), "Twitch Notifier");
    private final javax.swing.ImageIcon trayOff = new javax.swing.ImageIcon(getClass().getResource("/res/tray_off.png"), "Twitch Notifier");
    private final SystemTray tray = SystemTray.getSystemTray();
    
    /**
     * Creates new form TwitchView
     */
    public TwitchView(TwitchController twc, boolean undecorated) {
        this.twc = twc;
        this.setUndecorated(undecorated);
        initComponents();
        initLayers();
        setResizable(false);
        this.setIconImage(windowIcon.getImage());
        messageLabel.setVisible(false);
        setContentPanel(0);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        twitchLogo = new javax.swing.JLabel();
        layeredPane = new javax.swing.JLayeredPane();
        minimizeButton = new javax.swing.JButton();
        messageLabel = new javax.swing.JLabel();
        configButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(0, 0, 0));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        twitchLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/twitchlogo.png"))); // NOI18N
        twitchLogo.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                twitchLogoMouseDragged(evt);
            }
        });

        minimizeButton.setText("Minimize to tray");
        minimizeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minimizeButtonActionPerformed(evt);
            }
        });

        messageLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/error.png"))); // NOI18N
        messageLabel.setText("    ");

        configButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/config_open.png"))); // NOI18N
        configButton.setText("Configure");
        configButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(twitchLogo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(minimizeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(configButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(layeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 423, Short.MAX_VALUE)
                    .addComponent(messageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 423, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(twitchLogo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(minimizeButton))
                    .addComponent(layeredPane, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(messageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
                    .addComponent(configButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void minimizeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minimizeButtonActionPerformed
        System.out.println(evt.getActionCommand());
        if (!SystemTray.isSupported()) {
            showMessage(0, "Tray is not supported");
            return;
        }
        if(trayIcon == null){
            buildTray();
        }
        twc.toggleWindow(false);
    }//GEN-LAST:event_minimizeButtonActionPerformed

    public void minimizeWindow() {
        for(ActionListener al : minimizeButton.getActionListeners()){
            al.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Minimize to tray"));
        }
    }
    
    // Drag'n'drop logic
    private void twitchLogoMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_twitchLogoMouseDragged
        Point currPos = evt.getLocationOnScreen();
        this.setLocation(currPos.x-70, currPos.y-50);
        int x = (twc.getScreenNumber(this)>0) ? 54 : 237;
        int i = (currPos.y >= x) ? 233 : -174; // window offset
        twc.getConfigWindow().setLocation(currPos.x-70, currPos.y-i);
    }//GEN-LAST:event_twitchLogoMouseDragged

    private void configButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configButtonActionPerformed
        if(configButtonCounter++ % 2 == 0){ // hide it every 2nd click
            twc.showConfigWindow(true);
        } else {
            twc.showConfigWindow(false);
            if(configButtonCounter > 1) configButtonCounter = 0;
        }
    }//GEN-LAST:event_configButtonActionPerformed

    // xd is a initiate counter, trayIcons not loaded until object has finished constructing
    private int xd = 0;
    private void setTrayIcon(int l){
        if(xd++ > 0)
        trayIcon.setImage((l > 0 ? trayOn.getImage() : trayOff.getImage()));
        else if(xd > 2) xd = 0;
    }
    
    private void buildTray(){
        trayIcon = new TrayIcon(trayOff.getImage());
        final PopupMenu popMenu = new PopupMenu();
        // Create a popup menu components
        MenuItem restoreItem = new MenuItem("Restore");
        CheckboxMenuItem disableItem = new CheckboxMenuItem("Disable Notifications");
        MenuItem exitItem = new MenuItem("Exit");
        MenuItem resetPositionItem = new MenuItem("Reset Position");
        
        disableItem.setState(twc.getNotifications());
        
        popMenu.add(restoreItem);
        popMenu.add(disableItem);
        popMenu.addSeparator();
        popMenu.add(resetPositionItem);
        popMenu.addSeparator();
        popMenu.add(exitItem);
        
        trayIcon.setPopupMenu(popMenu);
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            showMessage(0, "Could not add tray icon!");
            return;
        }
        trayIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                twc.showOnline(listPanel.getOnline());
            }
        });
        restoreItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                twc.toggleWindow(true);
            }
        });
        disableItem.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                twc.setNotifications(!twc.getNotifications());
            }
        });
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tray.remove(trayIcon);
                twc.snapProperties();
                System.exit(4);
            }
        });
        resetPositionItem.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                twc.resetLocation(1);
                JOptionPane.showMessageDialog(null, "Position will be reset, please open program again.");
                System.exit(3);
            }
        });
        
    }
    
    public void fireUsername(String username){
        twc.generateOnlineList(username);
        loginPanel.enableControl(false);
    }
    
    public void setContentPanel(int i){
        switch(i){
            case 0:
                setTrayIcon(0);
                enableButton(true);
                listPanel.timerStop();
                listPanel.resetCounter();
                loginPanel.setVisible(true);
                layeredPane.moveToFront(loginPanel);
                listPanel.setVisible(false);
                break;
            case 1:
                setTrayIcon(1);
                listPanel.setVisible(true);
                layeredPane.moveToFront(listPanel);
                loginPanel.setVisible(false);
                break;
        }
    }
        
    public void enableButton(boolean b){
        loginPanel.enableControl(b);
    }
    
    public void hideLabel(){
        messageLabel.setVisible(false);
    }
    
    private void initLayers(){
        loginPanel = new twitchapplication.LoginPanel(twc);
        listPanel = new twitchapplication.ListPanel(twc);
        loginPanel.setBounds(0, 0, 380, 170);
        layeredPane.add(loginPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        listPanel.setBounds(0, 0, 400, 250);
        layeredPane.add(listPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);
    }
    
    public TrayIcon getTray(){
        if(trayIcon != null){
            return trayIcon;
        }
        return null;
    }
    
    private twitchapplication.ListPanel listPanel;
    private twitchapplication.LoginPanel loginPanel;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton configButton;
    private javax.swing.JLayeredPane layeredPane;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JButton minimizeButton;
    private javax.swing.JLabel twitchLogo;
    // End of variables declaration//GEN-END:variables

    /**
     * @param i 0 = error, 1 = info, 2 = blank, 3 = warning (exceptions)
     */
    public void showMessage(int i, String msg) {
        messageLabel.setText(msg);
        switch(i){
            case 0:
                messageLabel.setIcon(errorIcon);
                break;
            case 1:
                messageLabel.setIcon(infoIcon);
                break;
            case 2:
                messageLabel.setIcon(null);
                break;
            case 3:
                messageLabel.setIcon(warningIcon);
                break;
        }
        messageLabel.setVisible(true);
    }

    public void generateContent(ArrayList<Streamer> list) {
        listPanel.generateLists(list);
    }

    public void setUsername(String un) {
        loginPanel.setUsername(un);
    }
    
    public void fireLogin(){
        loginPanel.fireLogin();
    }
    
    public void setTimeout(int i){
        listPanel.setTimer(i);
    }
    
    public String getUsername() {
        return loginPanel.getUsername();
    }

    private int i = 0;
    public void setPopoutVideo(boolean b) {
        listPanel.setPopoutVideo(b);
        if(i++>0){
            listPanel.redrawList();
        }
            
    }

    public void setConfigIcon(boolean b) {
        if(b)
            configButton.setIcon(configCloseIcon);
        else
            configButton.setIcon(configOpenIcon);
    }

    public void setTrayTooltip(String tooltip) {
        if(trayIcon != null){
            trayIcon.setToolTip(tooltip);
        }
    }
}
