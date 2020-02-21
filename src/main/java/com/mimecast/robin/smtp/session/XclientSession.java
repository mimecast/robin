package com.mimecast.robin.smtp.session;

import com.mimecast.robin.config.ConfigMapper;
import com.mimecast.robin.config.client.CaseConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * XCLIENT session.
 *
 * <p>This provides access to XCLIENT session data.
 *
 * @see <a href="http://www.postfix.org/XCLIENT_README.html">Postfix XCLIENT</a>
 */
@SuppressWarnings("UnusedReturnValue")
public class XclientSession extends Session {

    private Map<String, String> xclient = new HashMap<>();

    /**
     * Constructs a new XclientSession instance.
     */
    public XclientSession() {
        super();
    }

    /**
     * Maps CaseConfig to this session.
     */
    @Override
    public void map(CaseConfig caseConfig) {
        new XclientConfigMapper(caseConfig).mapTo(this);
    }

    /**
     * Gets XCLIENT.
     *
     * @return XCLIENT parameters map.
     */
    public Map<String, String> getXclient() {
        return xclient;
    }

    /**
     * Sets XCLIENT parameters.
     *
     * @param xclient XCLIENT parameters map.
     * @return Self.
     */
    public XclientSession setXclient(Map<String, String> xclient) {
        this.xclient = xclient;
        return this;
    }

    /**
     * Mapper for CaseConfig to Session with XCLIENT.
     */
    public static class XclientConfigMapper extends ConfigMapper {

        /**
         * Mapper for CaseConfig to Session.
         *
         * @param config CaseConfig instance.
         */
        XclientConfigMapper(CaseConfig config) {
            super(config);
        }

        /**
         * Map configuration to given Session.
         *
         * @param session Session instance.
         */
        @SuppressWarnings("unchecked")
        @Override
        public void mapTo(Session session) {
            super.mapTo(session);

            Map<String, String> client = config.getMapProperty("xclient");
            if (client != null) {
                for (Map.Entry<String, String> pair : client.entrySet()) {
                    ((XclientSession) session).getXclient().put(pair.getKey(), pair.getValue());
                }
            }
        }
    }
}
