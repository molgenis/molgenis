@ECHO OFF
SET BACKEND=./backend-for-windows.conf
IF "%1"=="start" (
  docker-compose up -d
) ELSE (
 IF "%1"=="shutdown" (
   docker-compose down
 ) ELSE (
   IF "%1"=="terminate" (
     FOR /f "tokens=*" %%i IN ('docker ps -q') DO docker stop %%i
     docker system prune --all --force --volumes
   ) ELSE (
     ECHO Command not found
     ECHO Possible commands are: start, shutdown and terminate
   )
 )
)