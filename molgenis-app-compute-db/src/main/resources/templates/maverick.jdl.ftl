[
# General
JobType="Parametric";
ParameterStart=0;
ParameterStep=1;
Parameters=2;
VirtualOrganisation = "bbmri.nl";
DefaultNodeShallowRetryCount = 5;

# Executables, input and output
Executable = "/bin/sh";
Arguments = "maverick${pilotid}.sh";
StdOutput = "maverick.out";
StdError = "maverick.err";
InputSandbox = {"$HOME/maverick/maverick${pilotid}.sh"};
OutputSandbox = {"maverick.err","maverick.out"};
MyProxyServer = "px.grid.sara.nl";
RetryCount = 0;

# Only run in queues longer than 1440 minutes (36 hours)
# Requirements = (other.GlueCEPolicyMaxCPUTime >= 1440);
# && other.GlueCEPolicyMaxCPUTime >= 1440

Requirements = ((other.GlueCEInfoHostName =="creamce.gina.sara.nl")
#other.GlueCEInfoHostName == "cygnus.grid.rug.nl")
#other.GlueCEInfoHostName == "ce.lsg.hubrecht.eu" ||
#other.GlueCEInfoHostName == "ce.lsg.psy.vu.nl")
#other.GlueCEInfoHostName == "creamce.gina.sara.nl" ||
#other.GlueCEInfoHostName == "gazon.nikhef.nl")
#other.GlueCEInfoHostName == "juk.nikhef.nl" ||
#other.GlueCEInfoHostName == "gb-ce-amc.amc.nl" ||
#other.GlueCEInfoHostName == "gb-ce-emc.erasmusmc.nl" ||
#other.GlueCEInfoHostName == "gb-ce-kun.els.sara.nl" ||
#other.GlueCEInfoHostName == "gb-ce-lumc.lumc.nl" ||
#other.GlueCEInfoHostName == "gb-ce-nki.els.sara.nl" ||
#other.GlueCEInfoHostName == "gb-ce-tud.ewi.tudelft.nl")
&& (other.GlueHostArchitectureSMPSize >= 2)
&& (other.GlueCEPolicyMaxCPUTime >= 1440));
CERequirements="smpgranularity == 2";
CPUNumber=2;
]