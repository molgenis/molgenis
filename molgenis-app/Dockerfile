FROM molgenis/tomcat:tomcat-release-2019-03-28_15-25-46

# MOLGENIS war-file
ARG WAR_FILE

# Elasticsearch configuration
ENV elasticsearch.cluster.name=molgenis
ENV elasticsearch.transport.addresses=127.0.0.1:9300

# OpenCPU configuration
ENV opencpu.uri.scheme=http
ENV opencpu.uri.host=127.0.0.1
ENV opencpu.uri.port=8004
ENV opencpu.uri.path=/ocpu/

# Database configuration
ENV db_uri=jdbc:postgresql://127.0.0.1/molgenis
ENV db_user=molgenis
ENV db_password=molgenis

# MOLGENIS specific configuration
ENV admin.password=admin
ENV molgenis.home=/home/molgenis

# --no-cache allows users to install packages with an index that is updated and used on-the-fly and not cached locally
# This avoids the need to use --update and remove /var/cache/apk/* when done installing packages.
RUN apt-get update
RUN apt-get install -y openssl python3 fonts-dejavu-core

# Remove all (default) app from tomcat
RUN rm -fr ${CATALINA_HOME}/webapps/*

# Allow tomcat to read and exec files in webapps (sub)folder
RUN chmod -R 2755 webapps ${CATALINA_HOME}/webapps

# Copy war-file to webapps
COPY ${WAR_FILE} ${CATALINA_HOME}/webapps/ROOT.war

# add user molgenis to make sure the docker runs as user molgenis and not as root
# and set other molgenis specific requirements
RUN adduser --system --group --disabled-password molgenis

CMD ["catalina.sh", "run"]