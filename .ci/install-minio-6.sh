#!/usr/bin/env bash
sudo wget https://dl.minio.io/server/minio/release/linux-amd64/minio
sudo chmod +x minio
sudo export MINIO_ACCESS_KEY=molgenis
sudo export MINIO_SECRET_KEY=molgenis
sudo ./minio server /data