/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package twitchapplication;

import java.awt.event.ActionEvent;
import javax.swing.SwingWorker;

/**
 *
 * @author Toby
 */
public class LoginPanel extends javax.swing.JPanel {

    private TwitchController twc;

    /**
     * Creates new form LoginPanel
     * @param twc A reference to the 
     */
    public LoginPanel(TwitchController twc) {
        initComponents();
        progressBar.setVisible(false);
        this.twc = twc;
    }
    
    /**
     * Controls access to the "Go!" button
     * @param b The controlling boolean.
     */
    public void enableControl(boolean b) {
        goButton.setEnabled(b);
        usernameTextField.setEnabled(b);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        goButton = new javax.swing.JButton();
        usernameTextField = new javax.swing.JTextField();
        loadingLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        jLabel1.setText("Twitch Username:");

        goButton.setText("Go!");
        goButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                goButtonActionPerformed(evt);
            }
        });

        usernameTextField.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                usernameTextFieldCaretUpdate(evt);
            }
        });
        usernameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usernameTextFieldActionPerformed(evt);
            }
        });

        loadingLabel.setText("    ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(usernameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(goButton, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 129, Short.MAX_VALUE)
                        .addComponent(loadingLabel)
                        .addContainerGap(17, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(loadingLabel)
                .addGap(113, 113, 113))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usernameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(goButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(105, 105, 105))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void goButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_goButtonActionPerformed
        fireUsername();
    }//GEN-LAST:event_goButtonActionPerformed

    private void usernameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usernameTextFieldActionPerformed
        fireUsername();
    }//GEN-LAST:event_usernameTextFieldActionPerformed

    private void fireUsername(){
        class FieldWorker extends SwingWorker<String, Object> {
            @Override
            protected String doInBackground() {
                progressBar.setVisible(true);
                progressBar.setIndeterminate(true);
                twc.showMessage(MessageState.INFO, "Building list...");
                twc.fireUsername(usernameTextField.getText());
                return "Done.";
            }

            @Override
            protected void done() {
                progressBar.setVisible(false);
            }
        }
        new FieldWorker().execute();
    }
    private void usernameTextFieldCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_usernameTextFieldCaretUpdate
        // True when it can be remembered
        twc.allowUsernameToBeRemembered(!usernameTextField.getText().isEmpty());
    }//GEN-LAST:event_usernameTextFieldCaretUpdate

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton goButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel loadingLabel;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JTextField usernameTextField;
    // End of variables declaration//GEN-END:variables

    public void setUsername(String un) {
        usernameTextField.setText(un);
        usernameTextField.setCaretPosition(un.length());
    }
    
    public String getUsername(){
        return usernameTextField.getText();
    }

    public void fireLogin() {
       for(java.awt.event.ActionListener al : goButton.getActionListeners()){
           al.actionPerformed(new ActionEvent(this, 1011, "Go!"));
        }
    }
}
