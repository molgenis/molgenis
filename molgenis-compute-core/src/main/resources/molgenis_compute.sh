#
# This script runs the MOLGENIS/compute commandline with only the jars needed added to the CLASSPATH.
# To get relative path to this script use $(dirname -- "$0").
#

MCDIR=$( cd -P "$( dirname "$0" )" && pwd )
			
java -cp \
$(dirname -- "$0")/lib/molgenis-core-0.0.1-SNAPSHOT.jar:\
$(dirname -- "$0")/lib/molgenis-compute-core-0.0.1-SNAPSHOT.jar:\
$(dirname -- "$0")/lib/commons-cli-1.2.jar:\
$(dirname -- "$0")/lib/commons-io-2.4.jar:\
$(dirname -- "$0")/lib/freemarker-2.3.18.jar:\
$(dirname -- "$0")/lib/log4j-1.2.17.jar:\
$(dirname -- "$0")/lib/opencsv-2.3.jar \
$(dirname -- "$0")/lib/httpclient-4.2.5.jar \
org.molgenis.compute5.ComputeCommandLine \
$*