#!/bin/bash

export FRONTEND=molgenis/molgenis-frontend:8.3-stable
export BACKEND=./backend-for-mac.conf

function usage() {
  echo "*************************************************************************************"
  echo "* Usage                                                                             *"
  echo "*************************************************************************************"
  echo "* Please specify the action:                                                        *"
  echo "*   + start     = start the docker-compose stack                                    *"
  echo "*   + shutdown  = teardown the docker-compose stack                                 *"
  echo "*   + terminate = terminate and prune all system resources concerning docker        *"
  echo "* For example:                                                                      *"
  echo "*   docker-stack.bash start                                                         *"
  echo "*************************************************************************************"
}

if [ $# -eq 0 ]; then
  usage
  exit 1
fi

if [[ "$1" == "start" ]]; then
  docker-compose up -d
elif [[ "$1" == "shutdown" ]]; then
  docker-compose down
elif [[ "$1" == "terminate" ]]; then
  docker-compose down
  docker container stop $(docker container ls -aq)
  docker system prune --all --force --volumes
else
  usage
fi
