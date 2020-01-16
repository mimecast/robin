package com.mimecast.robin.main;

import com.mimecast.robin.Main;
import com.mimecast.robin.assertion.AssertException;
import com.mimecast.robin.config.client.CaseConfig;
import com.mimecast.robin.util.PathUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * CLI controller for email delivery client.
 *
 * @see Client
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public class ClientCLI {

    /**
     * Client usage.
     */
    public static final String USAGE = Main.USAGE + " --client";

    /**
     * Client description.
     */
    public static final String DESCRIPTION = "Email delivery client";

    /**
     * Main instance
     */
    private final Main main;

    /**
     * Constructs a new ClientCLI instance.
     *
     * @param main Main instance.
     */
    public ClientCLI(Main main) {
        this.main = main;

        Options options = options();

        Optional<CommandLine> opt = main.parseArgs(options);
        if (opt.isPresent()) {
            CommandLine cmd = opt.get();

            if (validateArgs(cmd)) {
                try {
                    send(cmd);
                } catch (AssertException | ConfigurationException e) {
                    main.log("Client error: " + e.getMessage());
                }
            } else {
                main.log("");
                main.optionsUsage(options);
            }
        }
    }

    /**
     * Instantiate client and send.
     */
    private void send(CommandLine cmd) throws AssertException, ConfigurationException {
        try {
            if (StringUtils.isNotBlank(cmd.getOptionValue("json"))) {
                new Client(cmd.getOptionValue("conf"))
                        .send(cmd.getOptionValue("json"));
            }
            else if (StringUtils.isNotBlank(cmd.getOptionValue("file"))) {
                Client client = new Client(cmd.getOptionValue("conf"));

                CaseConfig caseConfig = new CaseConfig();
                Map<String, Object> map = caseConfig.getMap();
                map.put("file", cmd.getOptionValue("file"));
                map.put("mx", Collections.singletonList(cmd.getOptionValue("mx")));
                map.put("mail", cmd.getOptionValue("mail"));
                map.put("rcpt", cmd.getOptionValue("rcpt"));
                if (StringUtils.isNotBlank(cmd.getOptionValue("port"))) {
                    map.put("port", cmd.getOptionValue("port"));
                }
                client.send(caseConfig);
            }
        } catch (IOException e) {
            main.log("Error reading: " + e.getMessage());
        }
    }

    /**
     * CLI getOptions.
     * <i>Listing order will be alphabetical</i>.
     */
    private Options options() {
        Options options = new Options();
        options.addOption("c", "conf", true,  "Path to configuration dir (Default: cfg/)");
        options.addOption("x", "mx",   true,  "Server to connect to");
        options.addOption("p", "port", true,  "Port to connect to");
        options.addOption("j", "json", true,  "Path to case file JSON");
        options.addOption("m", "mail", true,  "MAIL FROM address");
        options.addOption("f", "file", true,  "EML file to send");
        options.addOption("h", "help", false, "Show usage help");

        // Optional with unlimited values.
        Option rcptOpt = new Option("r", "rcpt", true, "RCPT TO address");
        rcptOpt.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(rcptOpt);

        return options;
    }

    /**
     * Validate arguments.
     *
     * @param cmd Commandline instance.
     * @return Boolean.
     */
    private boolean validateArgs(CommandLine cmd) {
        String mx = cmd.getOptionValue("mx");
        String mail = cmd.getOptionValue("mail");
        String[] rcpt = cmd.getOptionValues("rcpt");
        String file = cmd.getOptionValue("file");
        String json = cmd.getOptionValue("json");
        String conf = cmd.getOptionValue("conf");

        if (StringUtils.isBlank(json) && StringUtils.isBlank(file) ) {
            main.log("Config error: A file or a JSON are required");
            return false;
        }

        if (StringUtils.isNotBlank(json) && !PathUtils.isFile(json)) {
            main.log("Config error: JSON not found");
            return false;
        }

        if (StringUtils.isNotBlank(file) && !PathUtils.isFile(file)) {
            main.log("Config error: File not found");
            return false;
        }

        if (StringUtils.isNotBlank(file)) {
            if (mx == null) {
                main.log("Config error: MX required in file mode");
                return false;
            }

            if (mail == null) {
                main.log("Config error: MAIL required in file mode");
                return false;
            }

            if (rcpt == null) {
                main.log("Config error: RCPT required in file mode");
                return false;
            }
        }

        if (StringUtils.isBlank(conf) && !PathUtils.isDirectory(conf)) {
            main.log("Config error: Config directory not found");
            return false;
        }

        return true;
    }
}
