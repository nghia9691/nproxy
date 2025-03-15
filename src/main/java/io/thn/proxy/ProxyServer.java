package io.thn.proxy;

import io.thn.proxy.chain.SimpleChainedProxyAdapter;
import io.thn.proxy.chain.SimpleChainedProxyManager;
import org.littleshoot.proxy.ChainedProxyManager;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.logging.LogManager;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ProxyServer {

    private static final Logger LOG = LoggerFactory.getLogger(ProxyServer.class);

    public static void main(String[] args) throws Exception {
        try (InputStream logProps = ProxyServer.class.getResourceAsStream("/logging.properties")) {
            LogManager.getLogManager().readConfiguration(logProps);
        }

        var config = ProxyConfig.load();
        String upstreamServer = config.upstreamServer();
        var hasUpstreamServer = !upstreamServer.equalsIgnoreCase("localhost") && !upstreamServer.equals("127.0.0.1");

        ChainedProxyManager chainedProxyManager = null;
        HttpProxyServer internalServer;

        if (hasUpstreamServer) {
            LOG.info("Using upstream server: {}:{}", upstreamServer, config.upstreamPort());

            Pattern useProxyHostsPattern = null;
            try {
                useProxyHostsPattern = Pattern.compile(config.useProxyHostsRegex());
            } catch (PatternSyntaxException e) {
                LOG.error("Error parsing regex: {}. Use upstream proxy for all hosts.", e.getMessage());
            }

            var upstreamProxy = new SimpleChainedProxyAdapter(new InetSocketAddress(upstreamServer, config.upstreamPort()));
            var internalProxy = new SimpleChainedProxyAdapter(new InetSocketAddress("localhost", config.internalPort()));

            chainedProxyManager = new SimpleChainedProxyManager(upstreamProxy, internalProxy, useProxyHostsPattern);

            internalServer = DefaultHttpProxyServer.bootstrap().withPort(config.internalPort()).start();

        } else {
            internalServer = null;
            LOG.info("No upstream server configured.");
        }

        var proxyServer = DefaultHttpProxyServer.bootstrap()
                .withAddress(new InetSocketAddress("0.0.0.0", config.port()))
                .withChainProxyManager(chainedProxyManager)
                .start();

        // Add shutdown hook to stop the proxy servers
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutting down proxy servers...");
            if (internalServer != null) {
                internalServer.stop();
            }
            proxyServer.stop();
            LOG.info("Proxy servers stopped.");
        }));

        var latch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(latch::countDown));
        latch.await();
    }
}