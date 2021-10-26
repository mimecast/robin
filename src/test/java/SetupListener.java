import com.mimecast.robin.main.Foundation;
import com.mimecast.robin.util.PathUtils;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

import javax.naming.ConfigurationException;

/**
 * Junit setup listener.
 */
public class SetupListener implements LauncherSessionListener {

    /**
     * Initialiser instance.
     */
    private Initialiser initialiser;

    /**
     * Launcher session opened.
     *
     * @param session LauncherSession instance.
     */
    @Override
    public void launcherSessionOpened(LauncherSession session) {
        session.getLauncher().registerTestExecutionListeners(new TestExecutionListener() {
            @Override
            public void testPlanExecutionStarted(TestPlan testPlan) {
                if (initialiser == null) {
                    initialiser = new Initialiser();
                    initialiser.init();
                }
            }
        });
    }

    /**
     * Launcher session closed.
     *
     * @param session LauncherSession instance.
     */
    @Override
    public void launcherSessionClosed(LauncherSession session) {
        if (initialiser != null) {
            initialiser = null;
        }
    }

    /**
     * Initialiser class.
     * <p>Will initialise only if <code>init.path</code> system property is defined.
     * <p>Example VM options: -Dinit.path=src/main/resources/
     */
    static class Initialiser {

        /**
         * Initialise.
         */
        void init() {
            try {
                String initPath = System.getProperty("init.path");
                if (PathUtils.isDirectory(initPath)) {
                    Foundation.init(System.getProperty("init.path"));
                }
            } catch (ConfigurationException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
