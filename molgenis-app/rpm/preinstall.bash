#!/bin/bash

echo "########################################"
echo "PREINSTALL - MOLGENIS - started"
echo "########################################"
echo "[INFO] Cleanup old packages"
rm -rf /usr/local/share/molgenis/war/*.war
echo "[INFO] Add new user molgenis (in group: molgenis)"
if [[ $(getent passwd molgenis) ]]; then
  echo "[WARN] User 'molgenis' already exists"
else
  useradd molgenis:molgenis
fi
echo "----------------------------------------"
echo "PREINSTALL - MOLGENIS - finished"
echo "########################################"
