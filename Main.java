package com.library;

import com.library.threads.OverdueMonitor;
import com.library.ui.LoginFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Start the background monitor as a daemon so it does not keep the app alive.
        OverdueMonitor monitor = new OverdueMonitor();
        monitor.setDaemon(true);
        monitor.start();

        // Swing components should be created on the Event Dispatch Thread.
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
