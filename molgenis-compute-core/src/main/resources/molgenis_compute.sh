#
# This script runs the MOLGENIS/compute commandline with only the jars needed added to the CLASSPATH.
# To get relative path to this script use $(dirname -- "$0").
#

MCDIR=$( cd -P "$( dirname "$0" )" && pwd )

java -cp \
$MCDIR:\
$(dirname -- "$0")/lib/molgenis-core-0.0.1-SNAPSHOT.jar:\
$(dirname -- "$0")/lib/molgenis-compute-core-0.0.1-SNAPSHOT.jar:\
$(dirname -- "$0")/lib/commons-cli-1.2.jar:\
$(dirname -- "$0")/lib/commons-io-2.4.jar:\
$(dirname -- "$0")/lib/freemarker-2.3.18.jar:\
$(dirname -- "$0")/lib/log4j-1.2.17.jar:\
$(dirname -- "$0")/lib/opencsv-2.3.jar:\
$(dirname -- "$0")/lib/httpclient-4.2.5.jar:\
$(dirname -- "$0")/lib/httpcore-4.2.4.jar:\
$(dirname -- "$0")/lib/guava-14.0.1.jar:\
$(dirname -- "$0")/lib/commons-logging-1.1.1.jar:\
$(dirname -- "$0")/lib/gson-2.2.2.jar:\
$(dirname -- "$0")/lib/commons-lang3-3.1.jar \
org.molgenis.compute5.ComputeCommandLine \
$*