# MOLGENIS Compute 5.x Documentation

Licence: LGPLv3. http://www.molgenis.org

## Overview

Born from bioinformatics and life sciences, MOLGENIS compute is a flexible shell script framework to generate big data workflows that can run parallel on clusters and grids:

1. Design a workflow.csv with each step as a shell script 'protocol'
2. Generate and run jobs by iterating over parameters.csv and execute on a compute backend
3. (Optional) use standardized file and tool management for portable workflows

Typical command to generate and run jobs:

	molgenis -w workflow -p parameters.csv [-p moreparameters.csv]

* workflow = path to directory with workflow.csv or a workflow.csv file
* parameters.csv = file containing parameter values the workflow iterates over

If you don't like to use defaults, above translates to:

	molgenis --generate --run
			 --workflow workflow/workflow.csv \
			 --defaults workflow/defaults.csv \
			 --parameters parameters.csv \
			 --rundir ./run \
			 --backend pbs \
			 --database none

Alternatively you can configure molgenis compute stepwise:
	
	molgenis --workflow myworkflow
	molgenis --generate --parameters myparameters.csv
	molgenis --backend pbs
	molgenis --run

Download MOLGENIS compute as binary zip, see http://www.molgenis.org/wiki/ComputeStart for latest:

	wget http://molgenis26.target.rug.nl/downloads/molgenis_compute-latest.zip
	unzip molgenis_compute-latest.zip
	cd molgenis-compute-core-0.0.1-SNAPSHOT
	export PATH=${PATH}:${PWD}
	
Run your first workflow example on your local machine

	( Not available yet. Please follow the example below	)
	( in Section '1. Design a workflow' instead.			)
	(														)
	( mkdir demo1											)
	( cd demo1												)
	( cp -R ../molgenis-compute-5.x.x/demo1 .				)
	( molgenis -w workflow.csv -p parameters.csv -b local	)

Below details how to run.

## 1. Design a workflow

A typical workflow directory looks as follows:

	/protocols				#folder with bash script 'protocols'
	/protocols/step1.sh		#example of a protocol shell script
	/protocols/step2.sh		#example of a protocol shell script
	workflow.csv			#file listing steps and parameter flow
	defaults.csv			#default parameters for workflow.csv (optional)
	parameters.csv			#parameters you want to run analysis on

Create a new workflow directory having all of these files using:
	
	molgenis_compute --create
	cd myworkflow

or

	
	molgenis_compute --create <your_workflow_name>
	cd <your_workflow_name>
	
Generate your first workflow example on your local machine:

	molgenis_compute.sh -w workflow.csv -p parameters.csv -b local

Download example workflows:

	http://www.github.com/molgenis/molgenis-compute-workflows

### workflow.csv
Describes workflow steps and how they depend on each other via parameter input=source links.

Example workflow.csv (whitespaces will be trimmed):

	step,protocol,parameterMapping
	step1,protocols/step1.sh,in=user_input
	step2,protocols/step2.sh,wf=user_workflowName;date=user_creationDate;strings=step1_out
Explanation:

* step = unique name of the step within the workflow (a-zA-Z0-9)
* protocol = path to a protocol script. 
  * default, molgenis looks in current dir, same dir as workflow.csv and its 'protocols' dir
  * optionally, more protocols folders can be added via --protocols
* parameterMappings = mapping of input parameters from either previous step or parameters.csv, E.g.
  * step1.sh has input 'in' that is set from column 'sample' in parameters.csv 
  * step2.sh has input 'in' that takes output 'out' of step1, indicated by 'step1_' prefix

(__FUTURE WORK!__) You can include other workflows as sub-workflow. If different, you need to map the user parameters from parameters.csv to the user parameters expected by the sub-workflow. Output parameters will need additional prefix to point to the step in the nested workflow.

Example workflow.csv (white spaces will be trimmed):

	step,  protocol,              parameterMappings
	stepA, ../other/workflow.csv, in=user_input
	stepB, stepB.sh,              wf=user_workflowName;date=user_creationDate;strings=step1_out

Explanation:

* stepA has as protocol reference to another workflow (sub-workflow)
* parameters for stepA are a mapping of the 'user' parameters from master- to sub-workflow
* outputs of the nested workflow steps are namespaced:
	* stepB.sh has input 'in' that takes output 'out' from nested step1 in workflow stepA
	
### protocols 
Each step is described as a special shell script that we call 'protocol'. Its header describes: 

* (optional) resource needs via #MOLGENIS [mem=nG] [cores=n] [walltime=dd:hh:min]
* input #string with unique parameter values will be used to iterate over (see parameters.csv)
* input #list which will __*not*__ be used to iterate over (see parameters.csv)
* result #output which will be exposed to next steps (see workflow.csv)

MOLGENIS will generate a compute job for each unique combination of #string inputs provided in parameters.csv. See 'parameters.csv' for examples.

Example 'step1.sh':

	#MOLGENIS mem=2G cores=2 walltime=10:00:00
	#string in "some input"
	#string in2 "another input"
	#list in3 "list of values, each string a guest"
	#output out1 "some string that can be used in next steps"

	echo "started with inputs in=${in},in2=${in2}"
	echo "items in list in3:
	for i in in3
	do
		echo "* ${i}"
	done
	out1 = date "+%m-%d-%Y"

Parameter values can be used in #MOLGENIS headers, e.g.:

	#MOLGENIS mem=${mem}G

Optionally, you can use standardized file management and tool management functions to make your protocols portable across local, cluster and grid. For grid you need to use the pilot job database. See 'Pilot job database' and 'Advanced features'.

### defaults.csv
(Optional) Provide default values for the 'user' parameters. MOLGENIS will look in the same folder as your workflow.csv for a file names '[workflow].defaults.csv' or 'defaults.csv'. The format is equal to parameters.csv, described below.

Example defaults.csv (white space will be trimmed):

	tempdir, plink,
	/tmp,    /tools/plink18	

## 2. Run an analysis
A typical analysis directory while running looks as follows:

	parameters.csv		#contains the values for the analysis scripts
	run/				#directory where molgenis generates job scripts
	run/.molgenis		#file molgenis creates to keep track of settings
	analysis.sh			#(optional) saved version of your analysis commands

Example commands in your 'analysis.sh':

	#generate jobs in 'jobs' folder and run
	molgenis -w path/workflow -p parameters.csv [-p moreparameters.csv]

	#generate without running (so you can first inspect generated scripts)
	molgenis --generate -w path/workflow -p parameters.csv [-p moreparameters.csv]

	#run previously generated jobs from current jobs folder (default: ./jobs)
	molgenis --run

	#use non-default jobs directory
	molgenis -w path/workflow -p parameters.csv -j ./jobs2

### parameters.csv
Values for the workflow to iterate over can be passed as CSV file with parameter names in the header (a-zA-Z0-9, starting with a-zA-Z) and parameter values in each row. Use quotes to escape commas, e.g. "a,b". 

Each value is one of the following:

* a string
* an expression, e.g. "${p}file${c}", to create derived parameters such as directory paths
* a series i..j (where i and j are two integers), meaning from i to j including i and j
* a list of ';' separated values (may contain templates)

You can combine multiple parameter files: the values will be 'natural joined' based on overlapping columns. 

Example with two parameter files:

	molgenis -w path/to/workflow -p f1.csv -p f2.csv

Example f1.csv (white space will be trimmed):

	p0,  p2
	x,   1
	y,   2

Example f2.csv (white space will be trimmed):

	p1,  p2,    p3,  	p4
	v1,  1..2,  a;b,	file${p2}

Merged and expanded result for f1.csv + f2.csv:

	p0,  p1,  p2,   p3,	  p4
	x,   v1,  1,    a,	  file1	
	x,   v1,  1,    b,    file1
	y,   v1,  2,    a,	  file2
	y,   v1,  2,    b,    file2
	

### parameters in .properties format

Alternatively, parameters can be specified in the .properties file format. In this case, f1.csv will be replaced by f1.properties file and has the following content:

	p0= x, y
	p2= 1, 2

More complex parameter examples can combine values with template, as following:

	foo=    item1 , item2
	bar=    ${foo}, item3
	number= 123

#### Reserved words
MOLGENIS compute has some parameters that are part of its framework. Consequently, the names of these parameters are reserved words: user, port, interval, path, workflow, defaults, parameters, rundir, runid, backend, database, walltime, nodes, ppn, queue, mem. These parameters [[check: also the last 5?]] are available in each of the protocols, i.e. without mapping them in the workflow.csv.

### Generate and run jobs
Analysis jobs will be generated for each unique combination of '#string' inputs (see 'protocols').

Command:

	molgenis -w path/to/myworkflow -p parameters.csv [-b backend]

Examples on the number of jobs generated are shown below, based on on parameters.csv above:

Example step1.sh:

	#string p0
	#list p2
	will produce four jobs in total: two jobs for p0='x' and two jobs for p0='y'
	will have p2=[1,1] for p2=[1,1] for p0='x' en p2=[2,2] for p0='y'

Example step2.sh:

	#string p1
	#list p2
	will produce one job in total where p1='v1'
	will have p2=[1,1,2,2]
	
Example step3.sh: 

	#string p2
	#string p3
	#list p2
	will produce four jobs for p2,p3='1,a', p2,p3='1,b', p2,p3='2,a', p2,p3='2,b'
	will have p2=[1], p2=[1], p2=[2] and p2=[2] in the respective jobs

Experience has shown that saving the 'sh' is a good method to track your analysis configurations.

### Backends

MOLGENIS compute currently supports four backends:

	-b local    #will simply execute sequentially on the commandline
	-b pbs      #will use 'qsub' to submit jobs to pbs cluster job scheduler (default)
	-b custom   #will use the templates in the 'custom' folder in molgenis compute distro
	-b grid     #will use a 'job database' to submit jobs for the grid (requires -d)
	-d host		#configures the job database server, when you want to manage you jobs via db
				#default: none, or 'localhost' when using 'grid'.

### Using job database

Optionally you can manage your jobs via a job database. This is required when using -b grid because grid schedulers are notoriously unreliable. Therefore we implemented a best practice 'pilot job' database with help of http://www.ebiogrid.nl. In this system:

* the real jobs are submitted to a molgenis database (runs in background or central server)
* seperately, 'pilot' jobs are submitted to the grid
* each pilot job retrieves and runs real jobs (until it times-out)

Commands:	

	#start job database on localhost
	molgenis --database-start
	> molgenis job database started

	#generate jobs by putting them in pilot database
	molgenis -w workflow -p parameters.csv -b grid
	> submitted 20 jobs to database: localhost
	> runid=a3r4dtr5

	#submit 20 pilot jobs to the grid via pilot job database
	molgenis --pilot 20 user@grid.ui 
	> submitted 20 pilots to grid via user@grid.ui

You can use the pilot job database from another server as follows

	molgenis --database otherserver.com
	molgenis -w path/workflow -p parameters.csv -b grid 

You can submit pilots for another runid

	molgenis --pilot 20 user@grid.ui --id a3r4dtr5

### Monitoring jobs

You can list currently generated jobs via:

	molgenis --list
	
Example result:

	running 7 jobs (1 running, 6 queued, 0 completed, 0 failed)
	workflow:   path/to/workflow
	parameters: path/to/parameters.csv
	            path/to/moreparameters.csv
	backend:    grid
	rundir:		path/to/jobs
	database:	localhost (see http://localhost:8080/pilots for web view)
	jobid:		a3f4tg3de2
		
	job			host-id		status	time
	step1_x		1			R		00:00:10
	step1_y		2			Q		-
	step2_v1	3			Q		-
	step3_1_a	4			Q		-
	step3_1_b	5			Q		-
	step3_2_a	6			Q		-
	step3_2_b	7			Q		-

Explananation:

* (G) generated -- there is a *.sh file in the jobsdir
* (Q) queued -- jobs are on local/pbs/grid scheduled for execution
* (R.) running -- jobs are running
* (C.) completed -- jobs are completed without noticable errors
* (F) fail -- jobs have explicitly reported failure

Example result if no jobs are running, but some parameters are set:

	No jobs running. You can run generated jobs via molgenis --run

	workflow: 	path/to/previously/set/workflow
	parameters: path/to/previously/set/parameters
	backend: 	pbs
	rundir:		path/to/jobs
	host:		localhost

### Inspecting completed jobs

All logs are stored in rundir/[stepname].[out|err|fail|finished]

You can inspect the log files for your completed/failed jobs via

	molgenis --log	    		#merge of fail, error and output logs
	molgenis --error			#error logs for all steps
	molgenis --output			#output logs for all steps
	molgenis --fail				#fail logs for all steps (molgenis feature, see below)
	molgenis --fail step1_x  	#add step for individual log

You can inspect runtime parameters via

	more run/.molgenis #do not edit while running!

(__FUTURE WORK!__) : how to restart analyses.

## Making your workflows portable

We have introduced best practice procedures to make workflows portable between different machines, and between local, cluster and grid.

### Standard file management
You can use the following methods to standardize file management. (TODO: describe how to customize the behavior of getFile and putFile to work with local or remote file servers)

	getFile $myfile 		#retrieve file $myfile from storage to running dir
	putFile $myfile 		#store file $myfile from running dir to storage

### Standard tool management

MOLGENIS Compute does not handle tool dependencies. Hence when you want to execute an application, you will need to
* either provide the complete path to that application optionally as a parameter (not recommended)
* or modify your evironment, so the application can be found without specifying the absolute path (recommended)

We recommend the [Environment Modules](http://modules.sourceforge.net/) system for standardizing tool installation and management.
When your tool was installed and a module file was deployed for use with the Environment Modules system, you can use the `module` command to modify the environment for that tool:

	module avail				# Get a list of available tools and their versions.
	module load $mytool 			# Load $mytool before use without specifying a specific version.
	module load $mytool/$myversion		# Request a specific version of $mytool.
	module list				# List currently loaded modules.
	$mytool					# Execute $mytool.

In order to be able to trace back which version of which tool was used for a specific analysis, we recommend to always use:

	module load $mytool/$myversion		# Request a specific version of $mytool.
	module list				# List currently loaded modules.

in your protocols and to capture STDOUT into a log file. The `module list` command will then make sure the names and version numbers of all tools used in the analysis are listed in your logs.
See the [Environment Modules](http://modules.sourceforge.net/) system [documentation](http://modules.sourceforge.net/man/module.html) for instructions on how to create and deploy modules.

### Standard fail logging
Many commandline tools don't use out and error streams properly. Therefore, we also provide standard 'fail' logging methods which MOLGENIS will pick-up in monitoring.

	#put patricks examples here

## Commandline reference

Below options, --full_name or -short. Options can be combined:
	
	MOLGENIS compute 5.x

	Born from bioinformatics, MOLGENIS compute is a flexible shell script framework to 
	generate big data workflows that can run parallel on clusters and grids.
	
	Generate and run analysis using:
		
		molgenis -w path/workflow -p parameters.csv

	Below listing of options, --full_name and -short:

	#help						
	--help								prints this message

	#defaults
	molgenis					--help
	molgenis -w path -p path	--generate --run 
	molgenis -p path			--generate --run if -w has been set, otherwise error.

	#actions
	--generate -g				Generate jobs
	--run -r					Runs jobs from current run directory on current backend.
								When using --database this will return a 'id' for --pilot.
	--stop -s [step]			Stops running jobs. Optionally add step to stop only one job 
								(and dependent jobs).
	--list -l					List jobs, generated, queued, running, completed, failed
	--clean	-c					--stop + cleans the run folder from scripts + logs. 
								In case of database the jobs are removed there as well

	#workflow
	--workflow -w path			Loads workflow from path/workflow.csv and
								--defaults path/workflow.defaults.csv
	--workflow -w path.csv		Loads workflow from path.csv and
								--defaults path.defaults.csv
	--new -n [path]				Initializes template of a workflow in current folder or [path]

	#parameters
	--parameters -p path		Loads parameters from path.csv
								This option can be repeated to combine parameter files, e.g.:
								-p path1 -p path2, or equivalently, -p path1 path2
	--parameters -p path.csv	Identical to -p path

	#backend
	--backend -b name			Sets the backend, either 'local', 'pbs', 'pilot', 'custom'

	#pilot jobs
	--database -d host			(Optional) configure the pilot database. Default: none
								When 'localhost' molgenis will start database in background.
	--pilot	N user@grid.ui 		ssh to grid UI and submit pilot N jobs (e.g. N=20) using 
								credentials user@grid.ui. Optionally you can use '--id'.
	--runid 					Configure runid (optional using --pilot)
	--database-start -dbs		Start job database on localhost
	--database-end	-dbe		End job database on localhost

	#inspection
	--fail -f [step]			View all fail logs. Optionally only for particular step.
	--error -e [step]			View all error logs. Optionally only for particular step.
	--output -o [step]			View all output logs. Optionally only for particular step.
	--log -l [step]				View all logs combined. Optionally only for particular step.

	#jobs folder
	--rundir path				Sets the directory where the generated scripts should be 
								Stored, as well as runtime logs. Default: ./run

	#seldomly used options						
	--defaults path 			Loads workflow defaults parameter file. Default: defaults.csv.
								This option can be repeated to combine parameter files 
	--protocols path			Adds a directory to load protocol scripts from.
								Default: '[workflow]/protocols' and '[workflow]/.' and '.'
								This parameter can be repeated.
	--pilot-command				Put a custom command for pilot job submission
	--pilot-delay				Change the polling time (default: 10 seconds)

### List of reserved words

We have a list of words, which are reserved and cannot be used in compute to name parameters etc. These words are listed below:

	port
	interval
	workflow
	path
	defaults
	parameters
	rundir
	runid
	backend
	database
	walltime
	nodes
	ppn
	queue
	mem
	_NA
	password
	molgenisuser
	backenduser
	header
	footer
	submit
	autoid

## wish lists
* multiple default files

## Appendix: For developers

To get the compute source code:

Checkout 'molgenis'

	git clone https://www.github.com/molgenis/molgenis.git
	
Follow instructions in molgenis/README.md to compile code
	
To run compute in Eclipse, create a run configuration for ComputeCommandline with arguments 

	right click molgenis/molgenis-compute-core/

Example 1: split merge

	-w src/main/resources/workflows/splitmerge/workflow.csv 
	-p src/main/resources/workflows/splitmerge/parameters.csv 
	-j src/main/resources/workflows/splitmerge/out

Example 2: generate a burn down chart

	-p src/main/resources/workflows/burnDownChart/parameters.csv
	-j src/main/resources/workflows/burnDownChart/out

## Acknowledgements

We thank Martijn Dijkstra, George Byelas, Freerk van Dijk, Patrick Deelen, Alexandros Kanterakis and Morris Swertz for conceiving MOLGENIS compute. We thank (members of) BBMRI-NL Rainbow Project 2, Genome of the Netherlands, eBioGrid, Target, LifeLines, CTMM and members of the Genomics Coordination Center @ University Medical Center Groningen, The Netherlands for contributions and sponsoring of the development. This software is available under LGPLv3. Contact m.a.swertz AT rug.nl for questions and collaborations.

	

	
