@ECHO OFF

IF "%1"=="start" (
   docker-compose up -d
) ELSE (
  IF "%1"=="shutdown" (
    docker-compose down
  ) ELSE (
    IF "%1"=="terminate" (
      ECHO Stop running containers
      FOR /f "tokens=*" %%i IN ('docker ps -q') DO docker stop %%i
      ECHO Cleanup all the system
      docker system prune --all --force --volumes
    ) ELSE (
      ECHO Command not found
      ECHO Possible commands are: start, shutdown and terminate
    )
  )
)