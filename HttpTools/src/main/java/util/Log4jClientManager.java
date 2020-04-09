package util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ui.WindowMain;

public class Log4jClientManager {
    public boolean isTerm = false;

    public static Log4jClientManager getInstance() {
        return _instance;
    }

    public void init(boolean isTerm) throws FileNotFoundException, IOException {
        CLIENT = Logger.getLogger("Client");
        logExecute();
        this.isTerm = isTerm;
    }

    public void setLevelInfo() {
        CLIENT.setLevel(Level.INFO);
    }

    public void setLevelWar() {
        CLIENT.setLevel(Level.WARN);
    }

    public void setLevelDebug() {
        CLIENT.setLevel(Level.DEBUG);
    }

    public boolean isDebugEnabled() {
        return CLIENT.isDebugEnabled();
    }

    public boolean isInfoEnabled() {
        return CLIENT.isInfoEnabled();
    }

    public void debug(String str) {
        CLIENT.debug(str);
        if (!isTerm) {
            if (WindowMain.textArea.getText().length() > 1000000) {
                WindowMain.textArea.setText("");
            }
            WindowMain.textArea.append(str + System.getProperty("line.separator"));
        }
    }

    public void info(String str) {
        CLIENT.info(str);
        if (!isTerm) {
            if (WindowMain.textArea.getText().length() > 1000000) {
                WindowMain.textArea.setText("");
            }
            WindowMain.textArea.append(str + System.getProperty("line.separator"));
        }
    }

    public void error(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e);
        sb.append("\n");
        for (StackTraceElement ste : e.getStackTrace()) {
            sb.append("at ");
            sb.append(ste);
            sb.append("\n");
        }
        error(sb.toString());
    }

    public void error(String str) {
        try {
            CLIENT.error(str);
            if (!isTerm) {
                if (WindowMain.textArea.getText().length() > 1000000) {
                    WindowMain.textArea.setText("");
                }
                WindowMain.textArea.append(str + System.getProperty("line.separator"));
            }
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append(e);
            sb.append("\n");
            for (StackTraceElement ste : e.getStackTrace()) {
                sb.append("at ");
                sb.append(ste);
                sb.append("\n");
            }
            CLIENT.error(sb.toString());
        }
    }

    public static void logExecute() {
        scheduledSinglePoolLog = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("scheduledSinglePoolMin1");
                return thread;
            }
        });

        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {

                } catch (Exception e) {
                    Log4jClientManager.getInstance().error(e);
                }
            }
        };
        scheduledSinglePoolLog.scheduleWithFixedDelay(r, 0, 60 * 1000 * 1, TimeUnit.MILLISECONDS);
    }

    public static void stopLogExecute() {
        scheduledSinglePoolLog.shutdown();
        while (!scheduledSinglePoolLog.isTerminated()) {

        }
    }

    // ----------------------------------------------------------------

    static ScheduledExecutorService scheduledSinglePoolLog;

    private static Log4jClientManager _instance = new Log4jClientManager();
    private static Logger CLIENT;

    private Log4jClientManager() {}

}
