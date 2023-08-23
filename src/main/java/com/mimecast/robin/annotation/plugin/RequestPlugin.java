package com.mimecast.robin.annotation.plugin;

import com.mimecast.robin.annotation.Plugin;
import com.mimecast.robin.assertion.client.request.RequestExternalClient;
import com.mimecast.robin.main.Factories;

/**
 * Request plugin.
 *
 * <p>This is a basic HTTP request client.</p>
 * <p>Example configuration:</p>
 * <pre>
 * {
 *   "type": "request",
 *   "wait": 5,
 *   "delay": 5,
 *   "retry": 3,
 *   url: "https://robin.requestcatcher.com/",
 *   headers: {
 *     "Content-Type": "application/json",
 *     "Cache-Control": "no-cache",
 *     "Authorization": "Basic {$requestAuth}"
 *   },
 *   contentType: "application/json",
 *   file: "src/test/resources/cases/config/request/example.json5"
 * }
 * </pre>
 */
@SuppressWarnings("WeakerAccess")
@Plugin(priority = 103)
public final class RequestPlugin {

    /**
     * Constructs a new RequestPlugin instance.
     */
    public RequestPlugin() {
        Factories.putExternalClient("request", RequestExternalClient::new);
    }
}
