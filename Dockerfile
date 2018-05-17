FROM tomcat:8.5-jre8-alpine

ARG MOLGENIS_VERSION

# --no-cache allows users to install packages with an index that is updated and used on-the-fly and not cached locally
# This avoids the need to use --update and remove /var/cache/apk/* when done installing packages.
RUN apk --no-cache add openssl python3 \
	&& wget -O molgenis-app-${MOLGENIS_VERSION}.war "https://molgenis26.gcc.rug.nl/releases/molgenis/${MOLGENIS_VERSION}/molgenis-app-${MOLGENIS_VERSION}.war"

# Remove all (default) app from tomcat
RUN rm -fr ${CATALINA_HOME}/webapps/*

# Copy war to we
RUN mv molgenis-app-${MOLGENIS_VERSION}.war ${CATALINA_HOME}/webapps/ROOT.war

# Allow tomcat to read and exec files in webapps (sub)folder
RUN chmod -R 2755 webapps ${CATALINA_HOME}/webapps

RUN mkdir -p /opt/molgenis

EXPOSE 8080

CMD ["catalina.sh", "run"]