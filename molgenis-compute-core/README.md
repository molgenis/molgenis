# MOLGENIS Compute 5.x Documentation
Download compute as binary zip from http://www.molgenis.org/wiki/ComputeStart

	wget http://www.molgenis.org/releases/compute/compute_5.x.x.zip

## Basic concepts

MOLGENIS compute is a lightweight tool to create pipelines of analysis steps (in shell script) and execute in parallel on local servers, clusters and grids (tested on Linux and Mac). 

Compute in three steps:
 
1. Design a workflow (-w) of steps, each step pointing to a 'protocol' bash script
2. Create parameters (-p) to apply the workflow to
3. Generate tasks and submit for execution on cluster/grid backend (b)

Typical command:

	molgenis -w myworkflow -p myparameters.csv

Explanation:

	# myworkflow = directory with workflow or a workflow.csv file
	# myparameters = csv file containing parameter values per column

If you don't want to use defaults, above translates to:

	molgenis	--generate --submit
				--workflow_file myworkflow/workflow.csv \
				--workflow_defaults myworkflow/workflow.defaults.csv \
				--parameters myparameters.cvs \
				--jobdir jobs
				--backend pbs

Which is equal to shorthand notation:

	molgenis	-g -s
				-wf myworkflow/workflow.csv \
				-wd myworkflow/workflow.defaults.csv \
				-p myparameters.cvs \
				-j jobs
				-b pbs

You can also do this stepwise
	
	#set default backend
	molgenis -b pbs
	
	#set default workflow
	molgenis -w myworkflow

	#generate jobs
	molgenis -g -p myparameters.csv

	#submit jobs
	molgenis -s

Below commandline options and file types are explained.

## Commandline options

Below options, --full_name or -short. These can be combined.

	#actions
	--create -c [path]		creates empty workflow in current folder or in [path]	
	--generate -g			generates tasks. Default: '-w workflow.csv -p parameters.csv'
	--submit -s			submits last generated task to last used backend (see --backend)
	--monitor -m			monitor currently running jobs.
	
	#workflow
	--workflow -w [path]		translates to --workflow_file [path]/workflow.csv and
								--workflow_defaults [path]/workflow.defaults.csv
	--workflow -w [path.csv]	translates to --workflow_file [path].csv and
								--workflow_defaults [path].defaults.csv
	--workflow_file -wf [path]	points to workflow file
	--workflow_defauts -wd [path] 	points to workflow defaults file

	#backend
	--backend -b [name]		sets the backend, either 'local', 'pbs', 'pilot', 'custom'
	--host -h			sets the host, defaults to localhost

	#parameters
	--parameters -p	[path]		points to parameters file type [path].csv. 
					This parameter can be repeated. Then parameters will be merged.
	--parameters -p [path.csv]	identical to -p [path]
	
	#jobs folder
	--jobs -j [path]		indicate the output folder for the generated tasks + submit scripts. 								Default: ./tasks

## Workflow

A typical workflow directory looks as follows

	/protocols
	workflow.csv
	workflow.default.csv

### workflow.csv
Describes the steps of the workflows, and their parameter dependencies.

Example:

	step,	protocol,	parameters
	step1,	step1.sh,	sample=p.sample
	step2,	step2.sh,	in=step1.output

Explanation

* step = unique name of the step within the workflow.
* protocol = path to a protocol. Looks in folder of workflow.csv or workflow.csv/../protocols
* parameters = mapping of either USER.param or [step].output to inputs of scripts. E.g. step1.sh has input 'sample' that is set from the column 'sample' in parameters.csv; step2.sh has input 'in' that takes the output of step1. 
	
### protocols 
Each step is described as a protocol, which is a special bash script

Example:

	#MOLGENIS mem=2G cores=2 walltime=10:00:00
	#string group "a party"
	#string organizer "the individual organizing"
	#list guests "list of strings, each string a guest"
	#output invDate "invitation date"

	echo "Dear ${organizer},"
	echo "Please organize activities for the party '${group}' group."
	echo "List of your guests:"
	for g in guests
	do
		echo "* ${g}"
	done
	invDate = date + "%m-%d-%y"

Possible headers:
* #MOLGENIS [mem=nG] [cores=n] [walltime=dd:hh:min] -- resources needed by this script in terms of memory, cores and runtime
* #string name [description] -- input of type string; this script will run on each unique combination of group/organizer
* #list name [description] -- input of type list; guests will be grouped into a list based on group/organizer
* #ouput name [description] -- output to be used in next step.

### workflow.defaults.csv
(Optional) Decribes default values for the parameters.

## Generate and submit

### parameters.csv
Comma seperated file describing parameter values in each column.

Example:

	guest,		party,		organizer
	morris,		beer,		martijn
	pieter,		gataca,		pieter
	martijn,	beer,		martijn
	freerk,		gataca,		pieter

Explanation:

Each value is one of the following:

* a string (may contain Freemarker templates) or number
* a series i..j (where i and j are two integers), meaning from i to j including i and j
* a series of comma-separated numbers or strings (may contain Freemarker templates) "v1, v2" surrounded with quotes

## Advanced features

### Multiple parameter files

### Iteration strategies or 'folding'

### Runtime parameter value passing

### Including parameters and workflows via parameters.csv

### Custom backend
You can use a custom backend using option '''-b custom'''
To success you need to edit the following generator files

molgenis/custom.header.ftl
molgenis/custom.footer.ftl
molgenis/custom.submit.ftl

We are happy to add your custom backend as standard option to next version of compute :-)

## For developers

To get and code the compute source code:

Checkout 'molgenis' and 'molgenis_compute5'

	git clone https://www.github.com/molgenis/molgenis.git
	
Import into Eclipse 

	File -> Import ... -> Existing projects into workspace
	
Inside Eclipse create a run configuration for ComputeCommandline with arguments 

	right click molgenis/molgenis-compute-core/

Example 1: split merge

	-w src/main/resources/workflows/splitmerge/workflow.csv -p src/main/resources/workflows/splitmerge/parameters.csv -d src/main/resources/workflows/splitmerge/out

Example 2: generate a burn down chart

	-p src/main/resources/workflows/burnDownChart/parameters.csv
	-d /Users/mdijkstra/Documents/work/gitmaven/molgenis/molgenis-compute-core/src/main/resources/workflows/burnDownChart/out



	

	
