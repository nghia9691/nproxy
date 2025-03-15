package io.thn.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ProxyConfig {

    private static final Logger LOG = LoggerFactory.getLogger(ProxyConfig.class);
    private static final String CONFIG_FILE_NAME = "nproxy.properties";

    public static ProxyConfig load() {
        LOG.info("Loading proxy configuration...");

        Path currentDirPath = Paths.get(CONFIG_FILE_NAME);
        Path userDirPath = Paths.get(System.getProperty("user.home"), CONFIG_FILE_NAME);

        Properties props = new Properties();

        try {
            if (Files.exists(currentDirPath)) {
                try (FileInputStream fis = new FileInputStream(currentDirPath.toFile())) {
                    props.load(fis);
                    LOG.info("Loaded configuration from: {}", currentDirPath.toAbsolutePath());
                }
            } else if (Files.exists(userDirPath)) {
                try (FileInputStream fis = new FileInputStream(userDirPath.toFile())) {
                    props.load(fis);
                    LOG.info("Loaded configuration from: {}", userDirPath.toAbsolutePath());
                }
            } else {
                LOG.info("Configuration file '{}' not found in current directory or user home directory.", CONFIG_FILE_NAME);
                LOG.info("Using default configuration.");
            }
        } catch (IOException e) {
            LOG.error("Error loading configuration file: {}", e.getMessage());
        }

        return new ProxyConfig(props);

    }

    private final Properties props;

    private ProxyConfig(Properties props) {
        this.props = props;
    }

    public int internalPort() {
        return Integer.parseInt(props.getProperty("internal.port", "8889"));
    }

    public int port() {
        return Integer.parseInt(props.getProperty("port", "8888"));
    }

    public String upstreamServer() {
        return props.getProperty("upstream.server", "localhost");
    }

    public int upstreamPort() {
        return Integer.parseInt(props.getProperty("upstream.port", "8889"));
    }

    public String useProxyHostsRegex() {
        return props.getProperty("use.proxy.hosts.regex", ".*");
    }
}
