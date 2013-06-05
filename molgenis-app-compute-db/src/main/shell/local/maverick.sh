#!/bin/bash

rm molgenis.error.log
rm log.log
rm script.sh

curl  -s -S -u api:api -F status=started -F backend=localhost http://localhost:8080/api/pilot > script.sh

bash -l script.sh 2>&1 | tee -a log.log

FILE="molgenis.error.log"
if [ -f $FILE ];
then
	curl -s -S -u api:api -F status=nopulse -F log_file=@done.log -F failed_log_file=@molgenis.error.log http://localhost:8080/api/pilot
fi



