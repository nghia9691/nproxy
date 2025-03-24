FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN apt-get update && \
    apt-get install -y curl && \
    curl -fsSL https://pkgs.netbird.io/install.sh | sh && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

COPY target/nproxy-*-jar-with-dependencies.jar /app/nproxy.jar

EXPOSE 8888

CMD netbird service start & \
    netbird up --setup-key ${NETBIRD_SETUP_KEY} & \
    java -jar /app/nproxy.jar