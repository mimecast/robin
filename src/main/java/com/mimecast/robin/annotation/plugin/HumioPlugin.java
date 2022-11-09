package com.mimecast.robin.annotation.plugin;

import com.mimecast.robin.annotation.Plugin;
import com.mimecast.robin.assertion.client.humio.HumioExternalClient;
import com.mimecast.robin.main.Factories;

/**
 * Humio plugin.
 *
 * <p>Humio is a platform for consuming and monitoring event data ingested from logs.
 * The software is designed to be used by system administrators and DevOps to monitor large
 * quantities of data from logs, derive the individual events within those logs, and parse the
 * content in order to extract key data points from the source. The extracted and identified data
 * can be used to build graphs, identify anomalies, and create alerts to monitor your systems.</p>
 *
 * @see <a href="https://library.humio.com/training/getting-started/">Humio Getting Started</a>
 */
@SuppressWarnings("WeakerAccess")
@Plugin(priority = 102)
public final class HumioPlugin {

    /**
     * Constructs a new HumioPlugin instance.
     * <p>Sets extensions, behaviour and logs client.
     */
    public HumioPlugin() {
        Factories.putExternalClient("humio", HumioExternalClient::new);
    }
}
