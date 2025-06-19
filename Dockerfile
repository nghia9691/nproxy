FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN apt-get update && \
    apt-get install -y curl ca-certificates iptables && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Copy Tailscale binaries from the tailscale image on Docker Hub.
COPY --from=docker.io/tailscale/tailscale:stable /usr/local/bin/tailscaled /app/tailscaled
COPY --from=docker.io/tailscale/tailscale:stable /usr/local/bin/tailscale /app/tailscale
RUN mkdir -p /var/run/tailscale /var/cache/tailscale /var/lib/tailscale

COPY target/nproxy-*-jar-with-dependencies.jar /app/nproxy.jar
COPY start.sh /app/start.sh

EXPOSE 8888

CMD ["/app/start.sh"]