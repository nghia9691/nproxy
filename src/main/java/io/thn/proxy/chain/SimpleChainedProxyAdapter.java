package io.thn.proxy.chain;

import org.littleshoot.proxy.ChainedProxyAdapter;

import java.net.InetSocketAddress;

public class SimpleChainedProxyAdapter extends ChainedProxyAdapter {
    private final InetSocketAddress chainedProxyAddress;

    public SimpleChainedProxyAdapter(InetSocketAddress chainedProxyAddress) {
        this.chainedProxyAddress = chainedProxyAddress;
    }

    @Override
    public InetSocketAddress getChainedProxyAddress() {
        return chainedProxyAddress;
    }
}
