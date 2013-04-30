# MOLGENIS Compute 5.x Documentation

Licence: LGPLv3. http://www.molgenis.org

## Overview

Born from bioinformatics, MOLGENIS compute is a lightweight shell script framework to generate big data workflows that can run parallel on clusters and grids:
 
1. Design a workflow.csv with each step as a shell script 'protocol'
2. Generate jobs by iterating over parameters.csv and run on compute backend
3. (Optional) use standardized file and tool management for portable workflows

Typical command to generate and run jobs:

	molgenis -w workflow -p parameters.csv [-p moreparameters.csv]

* workflow = path to directory with workflow.csv or a workflow.csv file
* parameters.csv = file containing parameter values

If you don't like to use defaults, above translates to:

	molgenis	--generate --run
				--workflow workflow/workflow.csv \
				--defaults workflow/workflow.defaults.csv \
				--parameters parameters.csv \
				--jobdir jobs \
				--backend pbs

Alternatively can configure molgenis compute stepwise:
	
	molgenis --workflow myworkflow
	molgenis --generate --parameters myparameters.csv
	molgenis --backend pbs
	molgenis --run

Download MOLGENIS compute as binary zip, see http://www.molgenis.org/wiki/ComputeStart for latest:

	wget http://www.molgenis.org/releases/compute/molgenis-compute-5.x.x.zip
	unzip molgenis-compute-5.x.x.zip
	export PATH=$PATH:molgenis-compute-5.x.x

## 1. Design a workflow

A typical workflow directory looks as follows:

	/protocols				#folder with bash script 'protocols'
	/protocols/step1.sh		#example of a protocol shell script
	/protocols/step2.sh		#example of a protocol shell script
	workflow.csv			#file listing steps and parameter flow
	workflow.defaults.csv	#default parameters (optional)

Create a new workflow directory using:
	
	molgenis --create myworkflow
	cd myworkflow

Download example workflows:

	http://www.github.com/molgenis/molgenis-compute-workflows

### workflow.csv
Describes workflow steps and how they depend on each other via parameter input=source links.

Example workflow.csv (whitespaces will be trimmed):

	step,  protocol, parameters
	step1, step1.sh, in=user.sample;in2=user.project
	step2, step2.sh, in=step1.output

Explanation:

* step = unique name of the step within the workflow (a-zA-Z0-9)
* protocol = path to a protocol script. Looks in workflow.csv/. and workflow.csv/protocols
* parameters = mapping of input parameters from either previous step or parameters.csv, E.g.
  * step1.sh has input 'in' that is set from column 'sample' in parameters.csv 
  * step2.sh has input 'in' that takes the output of step1

(__FUTURE WORK!__) You can include other workflows as sub-workflow. Then you need to map the user variables (user.*) from parameters.csv to the workflow step (white spaces will be trimmed):

	step,  protocol,              parameters
	step1, ../other/workflow.csv, project=user.project;sample=user.sample
	step2, step2.sh,              in=step1.output
	
### protocols 
Each step is described as a 'protocol' shell script. Its header describes: 

* (optional) resource needs via #MOLGENIS [mem=nG] [cores=n] [walltime=dd:hh:min]
* input #string which unique values will be used to iterate over
* input #list which will __*not*__ be used to iterate over
* result #output which will be exposed to next steps

MOLGENIS will generate a compute job for each unique combinations of #string inputs provided in parameters.csv. See 'parameters.csv' for examples.

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
	out1 = date + "%m-%d-%y"

Optionally, you can use standardized file management and tool management functions to make your protocols portable across local, cluster and grid. See 'Advanced features'.

### workflow.defaults.csv
(Optional) Provide default values for the 'user.*' parameters. The format is equal to parameters.csv, described below.

Example workflow.defaults.csv (white space will be trimmed):

	tempdir, plink,
	/tmp,    /tools/plink18	

## 2. Run an analysis
A typical analysis directory while running looks as follows:

	parameters.csv		#contains the values for the analysis scripts
	/jobs				#directory where molgenis generates job scripts
	/jobs/.molgenis		#file molgenis creates to keep track of runtime values

Commands:

	#generate jobs in 'jobs' folder and run
	molgenis -w path/workflow -p parameters.csv [-p moreparameters.csv]

	#generate without running (so you can inspect generated scripts)
	molgenis --generate -w path/workflow -p parameters.csv [-p moreparameters.csv]

	#run generated jobs
	molgenis --run

	#use non-default jobs directory
	molgenis -w path/workflow -p parameters.csv -j jobs2

### parameters.csv
Values for the workflow to iterate over can be passed as CSV file with parameter names in the header (a-zA-Z0-9) and parameter values in each rown (use quotes to escape ',', e.g. "a,b"). 

Each value is one of the following:

* a string
* a template, e.g. ${p}, to create derived parameters
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

At runtime, after first expanding f1 and f2, and then merging, the parameters used are:

	p0,  p1,  p2,   p3,	  p4
	x,   v1,  1,    a,	  file1	
	x,   v1,  1,    b,    file1
	y,   v1,  2,    a,	  file2
	y,   v1,  2,    b,    file2

N.B. 'workflow', 'parameters', 'protocols', 'jobs', 'backend' and 'host' are reserved words that cannot be used as parameter name. These are used to pass all commandline parameters to parameters.csv before generation is started (allow reference in the protocols). This can also be used as alternative configuration method:

Example 'molgenis -p all_in_one_parameters.csv':

	workflow,              parameters,    protocols,          jobs, backend, host
	workflow/workflow.csv, f1.csv;f2.csv, workflow/protocols, jobs, grid,    localhost

### Generate and run jobs
Analysis jobs will be generated for each unique combination of '#string' inputs (see 'protocols').

Command:

	molgenis -w path/to/myworkflow -p parameters.csv [-b backend]

Examples on the number of jobs generated are shown below, based on on parameters.csv above:

Example step1.sh:

	#string p0
	#list p2
	will produce two jobs for p0='x' and p0='y'
	will have p2=[1,2] in both jobs

Example step2.sh:

	#string p1
	#list p2
	will produce one job for p1='v1'
	will have p2=[1,1,2,2]
	
Example step3.sh: 

	#string p2
	#string p3
	#list p2
	will produce four jobs for p2,p3='1,a', p2,p3='1,b', p2,p3='2,a', p2,p3='2,b'
	will have p2=[1], p2=[1], p2=[2] and p2=[2] in the respective jobs

### Backends

MOLGENIS compute currently supports four backends:

	-b local    #will simply execute sequentially on the commandline
	-b pbs      #will use 'qsub' to submit jobs to pbs cluster job scheduler (default)
	-b grid     #will use a 'pilot job database' to submit jobs for the grid
	-b custom   #will use the templates in the 'custom' folder in molgenis compute distro

NB: grid schedulers are notoriously unreliable. Therefore we implemented a best practice 'pilot job' database with help of http://www.ebiogrid.nl. In this system:

* the real jobs are submitted to a molgenis database (runs in background)
* seperately, 'pilot jobs' are submitted to the grid
* each pilot job retrieves and runs real jobs (until it times-out)

Command:
	
	#run jobs by putting them in pilot database (returns a key)
	molgenis -w workflow -p parameters.csv -b grid
	> key=1w1e3eewer3wrw3r

	#submit pilot jobs to the grid
	molgenis --pilot 20 user@grid.ui 

	#submit pilot jobs to the grid using another host and key
	molgenis --pilot 20 user@grid.ui -h myhost.com -key 1w1e3eewer3wrw3r

You can run the pilot job database on another server as follows

	#on other server, start pilot database (will be run in background)
	molgenis --backend grid

	#on the computer you want to run from, set --host to other server
	molgenis --host otherserver.com
	molgenis -b grid -w path/workflow -p parameters.csv

### Monitoring

You can monitor running jobs via:

	molgenis 
	
Example result:

	running 7 jobs (1 running, 6 queued, 0 completed, 0 errors)
	workflow:   path/to/workflow
	parameters: path/to/parameters.csv
	            path/to/moreparameters.csv
	backend:    grid
	jobdir:		path/to/jobs
	host:		localhost (see http://localhost:8080/pilots for web view)
	
	
	job			host-id		status	time
	step1_x		1			R		00:00:10
	step1_y		2			Q		-
	step2_v1	3			Q		-
	step3_1_a	4			Q		-
	step3_1_b	5			Q		-
	step3_2_a	6			Q		-
	step3_2_b	7			Q		-

Example result if no jobs are running, but some parameters are set:

	No jobs running. You can run generated jobs via molgenis --run

	workflow: 	path/to/previously/set/workflow
	parameters: path/to/previously/set/parameters
	backend: 	pbs
	jobdir:		path/to/jobs
	host:		localhost

You can inspect error logs via

	molgenis --log | more	#all logs

	molgenis --log step1_x  #individual log

You can inspect runtime parameters via

	more jobs/.molgenis #do not edit while running!

TODO: how to restart analyses.

## Making your workflows portable

We have introduced standard procedures to make workflows portable between machines, and between local, cluster and grid.

### Standard file management
You can use the following methods to standardize file management. (TODO: describe how to customize the behavior of getFile and putFile to work with local or remote file servers)

	getFile $myfile 		#retrieve file $myfile
	putFile $myfile 		#store file $myfile

### Standard tool management

In addition we recommend use of the 'module' system to standardize tool installation and management. See [ref].

	module load $mytool 	#load $mytool before use
	$mytool					#execute $mytool

See [WHERE?] for pre-configured modules to use.

### Standard error logging
Finally we also provide standard error logging methods which MOLGENIS will pick-up in monitoring:

	#put patricks examples here

## Commandline reference

Below options, --full_name or -short. Options can be combined:
	
	MOLGENIS compute 5.x

	Born from bioinformatics, MOLGENIS compute is a lightweight shell script framework to 
	generate big data workflows that can run parallel on clusters and grids:Generate and run 
	analysis using:

	molgenis -w path/workflow -p parameters.csv

	Below listing of options, --full_name and -short:

	#help						
	--help						prints this message

	#defaults
	molgenis -w path -p path	--generate --run 
	molgenis -p path			--generate --run if -w has been set before
	molgenis					--monitor

	#actions
	--generate -g				generates jobs
	--run -r					runs last generated jobs on last used backend
	--monitor -m				monitor currently running jobs.
	--create -c [path]			creates empty workflow in current folder, or in [path]
	--stop -s					stops running jobs; stops grid pilot database server
	
	#workflow
	--workflow -w path			Loads workflow from path/workflow.csv and
								--defaults path/workflow.defaults.csv
	--workflow -w path.csv		Loads workflow from path.csv and
								--defaults path.defaults.csv
	#parameters
	--parameters -p	path		Loads parameters from path.csv. 
								This option can be repeated to combine parameter files
	--parameters -p path.csv	identical to -p path

	#backend
	--backend -b name			sets the backend, either 'local', 'pbs', 'pilot', 'custom'
	--host -h host				sets the host for pilot database for grid (default: localhost)
	--pilot	N user@grid.ui		ssh to grid UI and submit pilot N jobs (e.g. N=20)
	--key key					use key for pilot jobs

	#jobs folder
	--jobdir -j path			sets the directory where the generated scripts should be 
								stored, as well as runtime logs. Default: ./jobs

	#seldomly used options						
	--defaults -d path 			Loads workflow defaults file
	--protocols path			Adds a directory to load protocol scripts from.
								Default: '[workflow]/protocols' and '[workflow]/.' and '.'
								This parameter can be repeated.


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

	

	
