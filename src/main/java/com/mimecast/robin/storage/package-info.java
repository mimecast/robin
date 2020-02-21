/**
 * Server Storage.
 *
 * <p>Provides an interface and local disk implementation for server incoming email storage.
 * <p>The default LocalStorageClient can be replaced with another implementation via Factories.
 * <br>Ideally this would be done in a plugin.
 *
 * <p>Example setting new storage client:
 * <pre>
 *     Factories.setStorageClient(RemoteStorageClient::new);
 * </pre>
 *
 * <p>Read more on plugins here: {@link com.mimecast.robin.annotation}
 */
package com.mimecast.robin.storage;
