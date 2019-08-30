package com.mimecast.robin.annotation.plugin;

import com.mimecast.robin.annotation.Plugin;
import com.mimecast.robin.main.Extensions;
import com.mimecast.robin.main.Factories;
import com.mimecast.robin.smtp.extension.Extension;
import com.mimecast.robin.smtp.extension.client.ClientXclient;
import com.mimecast.robin.smtp.extension.client.XclientBehaviour;
import com.mimecast.robin.smtp.extension.server.ServerXclient;
import com.mimecast.robin.smtp.session.XclientSession;

/**
 * XCLIENT plugin.
 * <p>XCLIENT is a SMTP extension developed by Postfix to provide the means to forge a sender.
 * <p>The intended purpose is for testing but if exposed by a real MTA it can be used to exploit the system.
 * <p>This software implements this without any security, but if used in production ensure the strictest control.
 *
 * <p>This will provide the XCLIENT behaviour and extension.
 * @link http://www.postfix.org/XCLIENT_README.html Postfix XCLIENT
 *
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
@SuppressWarnings("WeakerAccess")
@Plugin(priority=101)
public class XclientPlugin {

    /**
     * Constructs a new XclientPlugin instance and sets XCLIENT extension and behaviour.
     */
    public XclientPlugin() {
        Factories.setBehaviour(XclientBehaviour::new);
        Factories.setSession(XclientSession::new);

        Extensions.addExtension("xclient", new Extension(ServerXclient::new, ClientXclient::new));
    }
}
