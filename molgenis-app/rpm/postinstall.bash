#!/bin/bash

MOLGENIS_HOME=/home/molgenis/

DB_USER=molgenis
DB_PASSWORD=molgenis
ADMIN_PASSWORD=
MAIL_USER=molgenis
MAIL_PASSWORD=xxxx
MINIO_BUCKET_NAME=molgenis
MINIO_ENDPOINT=http://127.0.0.1:9000
MINIO_ACCESS_KEY=
MINIO_SECRET_KEY=
MINIO_REGION=

echo "########################################"
echo "POSTINSTALL - MOLGENIS - started"
echo "########################################"
echo "[INFO] Stop tomcat before upgrade"
service tomcat stop
echo "----------------------------------------"
echo "[INFO] Remove old installation"
rm -rf /usr/share/tomcat/webapps/ROOT*
echo "[INFO] Move the specific version to the ROOT.war"
echo "[INFO] Copy MOLGENIS war on tomcat"
echo "----------------------------------------"
mv /usr/local/share/molgenis/war/*.war /usr/local/share/molgenis/war/ROOT.war
cp -rp /usr/local/share/molgenis/war/ROOT.war /usr/share/tomcat/webapps/ROOT.war

echo "--------------------------------------------------"
echo "[INFO] Determine if there is a molgenis-server.properties"
if [[ -z ${INTERACTIVE} ]]
then
  "[WARN] Set interactive mode default to true"
  INTERACTIVE=true
fi
if [[ ! -f ${MOLGENIS_HOME}/molgenis-server.properties ]]
then
  if [[ ${INTERACTIVE} -eq "true" ]]
  then
    echo "[INFO] Configure [ molgenis-server.properties ]"
    echo "*****************************************"
    read -p "Enter database user: " DB_USER
    read -p "Enter database password: " DB_PASSWORD
    read -p "Enter admin password: " ADMIN_PASSWORD
    read -p "Enter mail username: " MAIL_USER
    read -p "Enter mail password: " MAIL_PASSWORD
    read -p "Enter Minio bucket name: " MINIO_BUCKET_NAME
    read -p "Enter Minio endpoint: " MINIO_ENDPOINT
    read -p "Enter Minio access key: " MINIO_ACCESS_KEY
    read -p "Enter Minio secret key: " MINIO_SECRET_KEY
    read -p "Enter Minio region: " MINIO_REGION
  else
    "[INFO] Running in non-interactive mode"
    "[INFO] Generate a [ molgenis-server.properties ] file"
  fi
  sed -e "s|__DB_USER__|${DB_USER}|" \
      -e "s|__DB_PASSWORD__|${DB_PASSWORD}|" \
      -e "s|__ADMIN_PASSWORD__|${ADMIN_PASSWORD}|" \
      -e "s|__MAIL_USER__|${MAIL_USER}|" \
      -e "s|__MAIL_PASSWORD__|${MAIL_PASSWORD}|" \
      -e "s|__MINIO_BUCKET_NAME__|${MINIO_BUCKET_NAME}|" \
      -e "s|__MINIO_ENDPOINT__|${MINIO_ENDPOINT}|" \
      -e "s|__MINIO_ACCESS_KEY__|${MINIO_ACCESS_KEY}|" \
      -e "s|__MINIO_SECRET_KEY__|${MINIO_SECRET_KEY}|" \
      -e "s|__MINIO_REGION__|${MINIO_REGION}|" \
      /usr/local/share/molgenis/templates/molgenis-server.properties > ${MOLGENIS_HOME}/molgenis-server.properties
else
  echo "[INFO] molgenis-server.properties already exists"
fi

echo "----------------------------------------"
echo "[INFO] Start tomcat"
service tomcat start

echo "----------------------------------------"
echo "POSTINSTALL - MOLGENIS - finished"
echo "########################################"
