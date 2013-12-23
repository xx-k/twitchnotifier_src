package twitchapplication;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.ArrayList;


public class MouseUtility {

    private static MouseUtility singleton;

    public static MouseUtility getInstance() {
        if (singleton == null) {
            singleton = new MouseUtility();
        }
        return singleton;
    }

    private MouseUtility() {}
    
    public boolean mouseAtTray(Point mousePosition){
        Rectangle bounds = getSafeScreenBounds(mousePosition);
        Point point = mousePosition;
        int x = point.x;
        int y = point.y;
        if (y < bounds.y) {
            y = bounds.y;
        } else if (y > bounds.y + bounds.height) {
            y = bounds.y + bounds.height;
        }
        return y < bounds.height;
    }

    /**
     * The following code was written by MadProgrammer
     * (http://stackoverflow.com/users/992484/madprogrammer) on Stackoverflow,
     * question 14431467 *
     */
    private Rectangle getSafeScreenBounds(Point pos) {
        Rectangle bounds = getScreenBoundsAt(pos);
        Insets insets = getScreenInsetsAt(pos);

        bounds.x += insets.left;
        bounds.y += insets.top;
        bounds.width -= (insets.left + insets.right);
        bounds.height -= (insets.top + insets.bottom);

        return bounds;
    }

    private Insets getScreenInsetsAt(Point pos) {
        GraphicsDevice gd = getGraphicsDeviceAt(pos);
        Insets insets = null;
        if (gd != null) {
            insets = Toolkit.getDefaultToolkit().getScreenInsets(gd.getDefaultConfiguration());
        }
        return insets;
    }

    private Rectangle getScreenBoundsAt(Point pos) {
        GraphicsDevice gd = getGraphicsDeviceAt(pos);
        Rectangle bounds = null;
        if (gd != null) {
            bounds = gd.getDefaultConfiguration().getBounds();
        }
        return bounds;
    }

    public GraphicsDevice getGraphicsDeviceAt(Point pos) {
        GraphicsDevice device = null;

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice lstGDs[] = ge.getScreenDevices();

        ArrayList<GraphicsDevice> lstDevices = new ArrayList<>(lstGDs.length);

        for (GraphicsDevice gd : lstGDs) {
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            Rectangle screenBounds = gc.getBounds();
            if (screenBounds.contains(pos)) {
                lstDevices.add(gd);
            }
        }
        if (lstDevices.size() > 0) {
            device = lstDevices.get(0);
        } else {
            device = ge.getDefaultScreenDevice();
        }
        return device;
    }
    /**
     * END OF CODE BLOCK *
     */
}
