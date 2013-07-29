#
# This script runs the MOLGENIS/compute commandline with only the jars needed added to the CLASSPATH.
# To get relative path to this script use $(dirname -- "$0").
#

MCDIR=$( cd -P "$( dirname "$0" )" && pwd )

java -cp \
$MCDIR:\
$(dirname -- "$0")/lib/activation-1.1.jar:\
$(dirname -- "$0")/lib/molgenis-core-0.0.1-SNAPSHOT.jar:\
$(dirname -- "$0")/lib/annotations-1.3.2.jar:\
$(dirname -- "$0")/lib/molgenis-core-ui-0.0.1-SNAPSHOT.jar:\
$(dirname -- "$0")/lib/aopalliance-1.0.jar:\
$(dirname -- "$0")/lib/molgenis-omx-auth-0.0.1-SNAPSHOT.jar:\
$(dirname -- "$0")/lib/commonj.sdo-2.1.1.jar:\
$(dirname -- "$0")/lib/molgenis-omx-core-0.0.1-SNAPSHOT.jar:\
$(dirname -- "$0")/lib/commons-cli-1.2.jar:\
$(dirname -- "$0")/lib/mysql-connector-java-5.1.25.jar:\
$(dirname -- "$0")/lib/commons-codec-1.5.jar:\
$(dirname -- "$0")/lib/opencsv-2.3.jar:\
$(dirname -- "$0")/lib/commons-fileupload-1.2.jar:\
$(dirname -- "$0")/lib/poi-3.9.jar:\
$(dirname -- "$0")/lib/commons-io-2.4.jar:\
$(dirname -- "$0")/lib/poi-ooxml-3.9.jar:\
$(dirname -- "$0")/lib/commons-lang3-3.1.jar:\
$(dirname -- "$0")/lib/poi-ooxml-schemas-3.9.jar:\
$(dirname -- "$0")/lib/commons-logging-1.1.1.jar:\
$(dirname -- "$0")/lib/quartz-1.8.6.jar:\
$(dirname -- "$0")/lib/dom4j-1.6.1.jar:\
$(dirname -- "$0")/lib/simplecaptcha-1.2.1.jar:\
$(dirname -- "$0")/lib/eclipselink-2.5.0.jar:\
$(dirname -- "$0")/lib/slf4j-api-1.7.2.jar:\
$(dirname -- "$0")/lib/slf4j-log4j12-1.7.2.jar:\
$(dirname -- "$0")/lib/freemarker-2.3.18.jar:\
$(dirname -- "$0")/lib/spring-aop-3.2.3.RELEASE.jar:\
$(dirname -- "$0")/lib/ganymed-ssh2-build210.jar:\
$(dirname -- "$0")/lib/spring-beans-3.2.3.RELEASE.jar:\
$(dirname -- "$0")/lib/gson-2.2.2.jar:\
$(dirname -- "$0")/lib/spring-context-3.2.3.RELEASE.jar:\
$(dirname -- "$0")/lib/guava-14.0.1.jar:\
$(dirname -- "$0")/lib/spring-context-support-3.2.3.RELEASE.jar:\
$(dirname -- "$0")/lib/hibernate-validator-4.2.0.Final.jar:\
$(dirname -- "$0")/lib/spring-core-3.2.3.RELEASE.jar:\
$(dirname -- "$0")/lib/httpclient-4.2.5.jar:\
$(dirname -- "$0")/lib/spring-expression-3.2.3.RELEASE.jar:\
$(dirname -- "$0")/lib/httpcore-4.2.4.jar:\
$(dirname -- "$0")/lib/spring-web-3.2.3.RELEASE.jar:\
$(dirname -- "$0")/lib/javax.mail-1.5.0.jar:\
$(dirname -- "$0")/lib/spring-webmvc-3.2.3.RELEASE.jar:\
$(dirname -- "$0")/lib/javax.persistence-2.1.0.jar:\
$(dirname -- "$0")/lib/stax-api-1.0.1.jar:\
$(dirname -- "$0")/lib/jsr305-2.0.1.jar:\
$(dirname -- "$0")/lib/validation-api-1.0.0.GA.jar:\
$(dirname -- "$0")/lib/log4j-1.2.17.jar:\
$(dirname -- "$0")/lib/xml-apis-1.4.01.jar:\
$(dirname -- "$0")/lib/molgenis-compute-core-0.0.1-SNAPSHOT.jar:\
$(dirname -- "$0")/lib/xmlbeans-2.3.0.jar \
org.molgenis.compute5.ComputeCommandLine \
$*