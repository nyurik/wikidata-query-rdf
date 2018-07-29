FROM openjdk:alpine

LABEL maintainer Nick Peihl <nick.peihl@elastic.co>

RUN apk add --no-cache \
    zip \
    bash \
    curl

ENV PATH=$PATH:/opt/wdqs/

ENV WQR_VERSION=0.3.0

WORKDIR /tmp


# FIXME !!!!   switch to sophox release dir
RUN \
    curl -sSL https://github.com/nickpeihl/wikidata-query-rdf/releases/download/${WQR_VERSION}/service-${WQR_VERSION}-SNAPSHOT-dist.zip -o service-${WQR_VERSION}-SNAPSHOT-dist.zip \
    && curl -sSL https://github.com/sophox/wikidata-query-rdf/releases/download/${WQR_VERSION}/extras.zip -o extras.zip \
    && mkdir -p /opt \
    && mkdir -p /var/log/wdqs \
    && unzip -q /tmp/service-${WQR_VERSION}-SNAPSHOT-dist.zip \
    && rm service-${WQR_VERSION}-SNAPSHOT-dist.zip \
    && mv service-${WQR_VERSION}-SNAPSHOT /opt/wdqs \
    && mkdir extras \
    && unzip -q /tmp/extras.zip -d extras \
    && rm extras.zip \
    && cp -r extras/* /opt/wdqs \
    && rm -rf extras

ARG heap_size=8g
ARG log_dir=/var/log/wdqs
ARG port=9999

ENV HOST="0.0.0.0" \
    PORT="9999" \
    DIR="/opt/wdqs" \
    HEAP_SIZE=$heap_size \
    LOG_CONFIG="" \
    LOG_DIR=$log_dir \
    MEMORY="-Xmx$heap_size" \
    GC_LOGS="-Xloggc:$log_dir/wdqs-blazegraph_jvm_gc.%p.log \
    -XX:+PrintGCDetails \
    -XX:+PrintGCDateStamps \
    -XX:+PrintGCTimeStamps \
    -XX:+PrintAdaptiveSizePolicy \
    -XX:+PrintReferenceGC \
    -XX:+PrintGCCause \
    -XX:+PrintGCApplicationStoppedTime \
    -XX:+PrintTenuringDistribution \
    -XX:+UseGCLogFileRotation \
    -XX:NumberOfGCLogFiles=10 \
    -XX:GCLogFileSize=20M" \
    EXTRA_JVM_OPTS="" \
    BLAZEGRAPH_OPTS="" \
    CONFIG_FILE="RWStore.docker.properties" \
    DEBUG=

WORKDIR /opt/wdqs

COPY ./dist/src/script/RWStore.docker.properties ./dist/src/script/whitelist.txt ./

EXPOSE 9999

ENTRYPOINT [ "runBlazegraph.sh" ]
