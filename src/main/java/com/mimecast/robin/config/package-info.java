/**
 * Configuration core.
 *
 * <p>Provides the configuration foundation and utilities.
 *
 * <p>Also provides accessors for components.
 *
 * <p>The Log4j2 XML filename can be configured via properties.json or a system property called <i>log4j2</i>.
 * <b>Example:</b>
 * <pre>java -jar robin.jar --server config/ -Dlog4j2=log4j2custom.xml</pre>
 *
 * <p>The properties.json filename can be configured via a system property called <i>properties</i>.
 * <b>Example:</b>
 * <pre>java -jar robin.jar --server config/ -Dproperties=properties-new.json</pre>
 */
package com.mimecast.robin.config;
