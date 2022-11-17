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
     * Initializer instance.
     */
    private Initializer initializer;

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
                if (initializer == null) {
                    initializer = new Initializer();
                    initializer.setUp();
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
        if (initializer != null) {
            initializer.tearDown();
            initializer = null;
        }
    }

    /**
     * Initializer class.
     * <p>Will initialise only if <code>init.path</code> system property is defined.
     * <p>Example VM options: -Dinit.path=cfg/
     */
    static class Initializer {

        /**
         * Set up.
         */
        void setUp() {
            try {
                String initPath = System.getProperty("init.path");

                if (PathUtils.isDirectory(initPath)) {
                    Foundation.init(initPath);
                }
            } catch (ConfigurationException e) {
                System.out.println(e.getMessage());
            }
        }

        /**
         * Tear down.
         */
        void tearDown() {
            // Do nothing.
        }
    }
}
