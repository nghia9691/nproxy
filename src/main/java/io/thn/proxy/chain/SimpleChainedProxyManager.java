package io.thn.proxy.chain;

import io.netty.handler.codec.http.HttpRequest;
import org.littleshoot.proxy.ChainedProxy;
import org.littleshoot.proxy.ChainedProxyManager;
import org.littleshoot.proxy.impl.ClientDetails;

import java.util.Queue;
import java.util.regex.Pattern;

public class SimpleChainedProxyManager implements ChainedProxyManager {

    private final ChainedProxy upstreamProxy;
    private final ChainedProxy internalProxy;
    private final Pattern useProxyHostsPattern;

    public SimpleChainedProxyManager(ChainedProxy upstreamProxy, ChainedProxy internalProxy, Pattern useProxyHostsPattern) {
        this.upstreamProxy = upstreamProxy;
        this.internalProxy = internalProxy;
        this.useProxyHostsPattern = useProxyHostsPattern;
    }

    @Override
    public void lookupChainedProxies(HttpRequest httpRequest, Queue<ChainedProxy> chainedProxies, ClientDetails clientDetails) {
        if (useProxy(getHost(httpRequest))) {
            chainedProxies.add(upstreamProxy);
        } else {
            chainedProxies.add(internalProxy);
        }
    }

    private boolean useProxy(String host) {
        return useProxyHostsPattern == null || useProxyHostsPattern.matcher(host).matches();
    }

    private String getHost(io.netty.handler.codec.http.HttpRequest httpRequest) {
        String[] tokens = httpRequest.uri().split("/+", 3);
        String host = tokens.length == 1 ? tokens[0] : tokens[1];
        tokens = host.split(":", 2);
        host = tokens[0];
        return host;
    }
}
