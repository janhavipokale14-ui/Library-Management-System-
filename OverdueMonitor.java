package com.library.threads;

import com.library.dao.LoanDAO;
import java.sql.SQLException;
import java.util.List;

public class OverdueMonitor extends Thread {
    private volatile boolean running = true;
    private final LoanDAO loanDAO = new LoanDAO();

    public OverdueMonitor() {
        super("Overdue-Monitor-Thread");
    }

    @Override
    public void run() {
        while (running) {
            try {
                List<String> overdue = loanDAO.getOverdueDescriptions();
                if (!overdue.isEmpty()) {
                    System.out.println("[Overdue monitor] Overdue books:");
                    overdue.forEach(System.out::println);
                }
                Thread.sleep(60_000); // Check every 60 seconds.
            } catch (SQLException ex) {
                System.err.println("[Overdue monitor] Database error: " + ex.getMessage());
                try {
                    Thread.sleep(60_000);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    running = false;
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                running = false;
            }
        }
    }

    public void stopMonitor() {
        running = false;
        interrupt();
    }
}
