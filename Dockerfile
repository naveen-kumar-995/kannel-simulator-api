# Use OpenJDK 21 as base image
FROM eclipse-temurin:21-jdk

LABEL maintainer="PTSL"

ENV TZ=Asia/Kolkata

# Install required packages
RUN apt-get update && \
    apt-get install -y curl tar vim && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Download and install Tomcat
ENV TOMCAT_VERSION=10.1.41

RUN curl -fsSL https://archive.apache.org/dist/tomcat/tomcat-10/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz -o /tmp/tomcat.tar.gz && \
    mkdir -p /opt/tomcat && \
    tar -xzf /tmp/tomcat.tar.gz -C /opt/tomcat --strip-components=1 && \
    rm -f /tmp/tomcat.tar.gz

# Allow encoded slash
RUN echo "org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true" >> \
    /opt/tomcat/conf/catalina.properties

# Application directories
RUN mkdir -p \
    /opt/apps/fallback_files \
    /opt/apps/logs

# Remove default webapps
RUN rm -rf /opt/tomcat/webapps/*

# Replace Tomcat configuration
COPY server.xml /opt/tomcat/conf/server.xml
COPY logback.xml /opt/tomcat/conf/logback.xml

# Deploy application
COPY kannel-simulator-api.war /opt/tomcat/webapps/ROOT.war

WORKDIR /opt/tomcat

EXPOSE 8080

ENV CATALINA_OPTS="-Dlogback.configurationFile=/opt/tomcat/conf/logback.xml -DLOG_HOME=/opt/apps/logs"

ENTRYPOINT ["bin/catalina.sh"]

CMD ["run"]