import java.awt.EventQueue;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.log4j.xml.DOMConfigurator;
import logic.LogicListener;
import ui.WindowMain;
import util.Log4jClientManager;
import util.MiscUtils;

public class Main {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        StringBuilder pathBuilder = new StringBuilder(
                System.getProperty("user.dir") + File.separator + "config" + File.separator);
        if (MiscUtils.isIDEEnvironment()) {
            pathBuilder.append("log4j_devel.xml");
        } else {
            pathBuilder.append("log4j_server.xml");
        }
        DOMConfigurator.configureAndWatch(pathBuilder.toString());

        // 终端参数说明:

        boolean isTerm = false;
        if (args.length >= 1) {
            isTerm = true;
        }


        Log4jClientManager.getInstance().init(isTerm);

        if (isTerm) {
            LogicListener.start(args);
        } else {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    try {
                        final WindowMain frame = new WindowMain();
                        frame.setVisible(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

}
