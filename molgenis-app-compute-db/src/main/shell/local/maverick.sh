rm log.log
rm script.sh
curl  -F status=started -F backend=localhost http://localhost:8080/api/pilot > script.sh
bash -l script.sh 2>&1 | tee -a log.log
