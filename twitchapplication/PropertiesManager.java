/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package twitchapplication;

/**
 *
 * @author Toby
 */
public class PropertiesManager {
    private static String[][] properties = 
    {{"DisableNotifactions", "false"},
     {"User", ""},
     {"RememberUser", ""},
     {"AutoLogin", "false"},
     {"PosX", "0"},
     {"PosY", "0"},
     {"TimerUpdate", "30"},
     {"StartMinimized", "false"},
     {"PopoutVideo", "false"},
     {"RememberPosition", "false"},
     {"UndecoratedWindow", "true"}};
    
    public static String[][] getProperties(){
        return properties;
    }
    
}
