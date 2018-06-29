FROM tomcat:8.5-jre8-alpine

# MOLGENIS war-file
ARG WAR_FILE

# Elasticsearch configuration
ENV elasticsearch.cluster.name=molgenis
ENV elasticsearch.transport.addresses=127.0.0.1:9300

# OpenCPU configuration
ENV opencpu.uri.scheme=http
ENV opencpu.uri.host=localhost
ENV opencpu.uri.port=8004
ENV opencpu.uri.path=/ocpu/

# Database configuration
ENV db_driver=org.postgresql.Driver
ENV db_uri=jdbc:postgresql://localhost/molgenis
ENV db_user=molgenis
ENV db_password=molgenis

# MOLGENIS specific configuration
ENV admin.password=admin
ENV molgenis.home=/usr/share/molgenis/data

# --no-cache allows users to install packages with an index that is updated and used on-the-fly and not cached locally
# This avoids the need to use --update and remove /var/cache/apk/* when done installing packages.
RUN apk --no-cache add openssl python3 ttf-dejavu

# Remove all (default) app from tomcat
RUN rm -fr ${CATALINA_HOME}/webapps/*

# Allow tomcat to read and exec files in webapps (sub)folder
RUN chmod -R 2755 webapps ${CATALINA_HOME}/webapps

# Copy war-file to webapps
COPY ${WAR_FILE} ${CATALINA_HOME}/webapps/ROOT.war

# add user molgenis to make sure the docker runs as user molgenis and not as root
# and set other molgenis specific requirements
RUN useradd molgenis
RUN mkdir -p /usr/share/molgenis/data

# Switch to user molgenis
USER molgenis

EXPOSE 8080

CMD ["catalina.sh", "run"]