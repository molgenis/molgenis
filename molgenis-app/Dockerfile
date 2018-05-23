FROM tomcat:8.5-jre8-alpine

# --no-cache allows users to install packages with an index that is updated and used on-the-fly and not cached locally
# This avoids the need to use --update and remove /var/cache/apk/* when done installing packages.
RUN apk --no-cache add openssl python3 ttf-dejavu

# Remove all (default) app from tomcat
RUN rm -fr ${CATALINA_HOME}/webapps/*

# Allow tomcat to read and exec files in webapps (sub)folder
RUN chmod -R 2755 webapps ${CATALINA_HOME}/webapps

RUN mkdir -p /opt/molgenis

VOLUME /opt/molgenis

EXPOSE 8080

ARG WAR_FILE
COPY ${WAR_FILE} ${CATALINA_HOME}/webapps/ROOT.war

CMD ["catalina.sh", "run"]