#!/usr/bin/env bash
wget https://dl.minio.io/server/minio/release/linux-amd64/minio
chmod +x minio
export MINIO_ACCESS_KEY=molgenis
export MINIO_SECRET_KEY=molgenis
./minio server /data