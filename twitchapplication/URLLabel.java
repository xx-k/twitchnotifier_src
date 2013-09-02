package twitchapplication;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import javax.swing.JLabel;

/**
 *
 * @author Toby
 */
public class URLLabel extends JLabel {
    
    private String URL;
    
    public URLLabel(){
        super();
        setForeground(Color.black);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        addMouseListener(new URLOpenAdapter());
    }
    

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    private void setActive(boolean b){
        if(b){
            setForeground(Color.blue);
        } else {
            setForeground(Color.black);
        }
    }
    
    //this is used to underline the text
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Insets insets = getInsets();

        int left = insets.left;
        if (getIcon() != null) {
            left += getIcon().getIconWidth() + getIconTextGap();
        }

        g.drawLine(left, getHeight() - 1 - insets.bottom,
                (int) getPreferredSize().getWidth()
                - insets.right, getHeight() - 1 - insets.bottom);
    }

    private class URLOpenAdapter extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(new URI(URL));
                } catch (Throwable t) {
                }
            }
        }
        
        @Override
        public void mouseEntered(MouseEvent e){
            setActive(true);
        }
        
        @Override
        public void mouseExited(MouseEvent e){
            setActive(false);
        }
    }

    
}
