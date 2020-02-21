/**
 * Plugin core.
 *
 * <p>Provides an annotation interface for plugin loading.
 *
 * <p>Plugins can be used to overide primary components to add functionality.
 * <br>A plugin is provided that provides SMTP XCLIENT support like in Postfix.
 *
 * <p>New plugins may be added in the package com.mimecast.robin.annotation.plugin.
 * <br>Plugins are loaded in order of priority thus any plugin depending on another must have a higher priority.
 * <br>Plugins of the same priority are loaded in random order.
 *
 * <p>Example plugin with custom session, storage client, behaviour and server and client extension:
 * <pre>
 *     &#64;Plugin(priority = 102)
 *     public class CustomPlugin {
 *
 *         public CustomPlugin() {
 *             Factories.setSession(CustomSession::new);
 *             Factories.setStorageClient(CustomStorageClient::new);
 *             Factories.setBehaviour(CustomBehaviour::new);
 *             Extensions.addExtension("custom", new Extension(ServerCustom::new, ClientCustom::new));
 *         }
 *     }
 * </pre>
 *
 * @see <a href="http://www.postfix.org/XCLIENT_README.html">Postfix XCLIENT</a>
 */
package com.mimecast.robin.annotation;
