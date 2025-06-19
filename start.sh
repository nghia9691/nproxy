#!/bin/sh

/app/tailscaled --tun=userspace-networking --state=/var/lib/tailscale/tailscaled.state --socket=/var/run/tailscale/tailscaled.sock &
/app/tailscale up --auth-key=${TAILSCALE_AUTHKEY} --hostname=nproxy-app
java -jar /app/nproxy.jar

