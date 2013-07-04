#
# This script runs the MOLGENIS/compute commandline with only the jars needed added to the CLASSPATH.
# To get relative path to this script use $(dirname -- "$0").
#

MCDIR=$( cd -P "$( dirname "$0" )" && pwd )
cd $MCDIR

java -cp \
lib/molgenis-core-0.0.1-SNAPSHOT.jar:\
lib/molgenis-compute-core-0.0.1-SNAPSHOT.jar:\
lib/commons-cli-1.2.jar:\
lib/commons-io-2.4.jar:\
lib/freemarker-2.3.18.jar:\
lib/log4j-1.2.17.jar:\
lib/opencsv-2.3.jar:\
lib/httpclient-4.2.5.jar:\
lib/httpcore-4.2.4.jar:\
lib/guava-14.0.1.jar:\
lib/commons-logging-1.1.1.jar:\
lib/gson-2.2.2.jar:\
lib/commons-lang3-3.1.jar \
org.molgenis.compute5.ComputeCommandLine \
$*