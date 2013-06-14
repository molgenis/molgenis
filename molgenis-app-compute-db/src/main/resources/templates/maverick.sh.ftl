#!/bin/bash

#------- data transfer

#<#noparse>
getRemoteLocation()
{
    ARGS=($@)
    myFile=${ARGS[0]}
    remoteFile=srm://srm.grid.sara.nl/pnfs/grid.sara.nl/data/bbmri.nl/RP2${myFile:`expr length $TMPDIR`}
    echo $remoteFile
}

getFile()
{
    ARGS=($@)
    NUMBER="${#ARGS[@]}";
    if [ "$NUMBER" -eq "1" ]
	then

	myFile=${ARGS[0]}
	remoteFile=`getRemoteLocation $myFile`

	# 1. myPath = getPath( myFile ) will strip off the file name and return the path
	mkdir -p $(dirname "$myFile")
	
	# 2. cp srm:.../remoteFile myFile
	echo "srmcp -server_mode=passive $remoteFile file:///$myFile"
	srmcp -server_mode=passive $remoteFile file:///$myFile
	chmod 755 $myFile

	else
	echo "Example usage: getData \"\$TMPDIR/datadir/myfile.txt\""
	fi
}

putFile()
{
    ARGS=($@)
    NUMBER="${#ARGS[@]}";
    if [ "$NUMBER" -eq "1" ]
	then
	myFile=${ARGS[0]}
	remoteFile=`getRemoteLocation $myFile`
	echo "srmrm $remoteFile"
	srmrm $remoteFile
	echo "srmcp -server_mode=passive file:///$myFile $remoteFile"
	srmcp -server_mode=passive file:///$myFile $remoteFile
		returnCode=$?

		echo "srmcopy: ${returnCode}"

		if [ $returnCode -ne 0 ]
		then
			exit 1	
		fi	
	else
	echo "Example usage: putData \"\$TMPDIR/datadir/myfile.txt\""
	fi
}

export -f getRemoteLocation
export -f getFile
export -f putFile

#-------end transfer

export MODULEPATH=${VO_BBMRI_NL_SW_DIR}/modules/:${MODULEPATH}
export WORKDIR=$TMPDIR

check_process(){
        # check the args
        if [ "$1" = "" ];
        then
                return 0
        fi

        #PROCESS_NUM => get the process number regarding the given thread name
        PROCESS_NUM=$(ps aux | grep "$1" | grep -v "grep" | wc -l)
        if [ $PROCESS_NUM -eq 1 ];
        then
                return 1
        else
                return 0
        fi
}
#</#noparse>
SERVER=${SERVER}

curl  -s -S -u api:api -F pilotid=${pilotid} -F status=started -F backend=ui.grid.sara.nl http://$SERVER:8080/api/pilot > script.sh
bash -l script.sh 2>&1 | tee -a log.log &

#to give some time to start the process
sleep 20

#now, we try to check the process 3 times
COUNTER=0

# check wheter the instance of thread exsits
while [ 1 ] ; do
        echo 'begin checking...'
        check_process "script.sh" # the thread name
        CHECK_RET=$?
        if [ $CHECK_RET -eq 0 ]; # none exist
        then
                COUNTER=$((COUNTER+1))
                echo "NOT RUNNING $COUNTER"

                #time to make sure that job reported back to db
                if [ $COUNTER -eq 3 ];
                then
                	echo 'FAILED 3 TIMES'
                	cp log.log inter.log
                	curl -s -S -u api:api -F pilotid=${pilotid} -F status=nopulse -F log_file=@inter.log http://$SERVER:8080/api/pilot
                	#kill the actual analysis, which does not guarantie that the process is killed
	                #because it can be finished till this point
	                kill `ps aux | grep 'script.sh' | grep -v 'grep' | cut -d ' ' -f 2,3,4,5,6,7,8`
	                exit 0
                fi
        elif [ $CHECK_RET -eq 1 ];
        then
                echo 'RUNNING'
                cp log.log inter.log
                curl -s -S -u api:api -F pilotid=${pilotid} -F status=pulse -F log_file=@inter.log http://$SERVER:8080/api/pilot
        fi
        #this sleep can be modified depending on how often, you like to receive the job status
        #it also depends on the number of running jobs, more jobs -> bigger interval
        sleep 10
done
