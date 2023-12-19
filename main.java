import javax.swing.SwingUtilities;

import Visuals.MainWindow;

public class main {
    public static void main(String[] args){
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MainWindow();
            }
        });
    }

}