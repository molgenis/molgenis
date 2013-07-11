Molgenis Compute 5 User Guide
===============================
George Byelas <h.v.byelas@gmail.com>
v0.1, July 2013:
Rewritten for version 1.0 release.
:toc:
:icons:
:numbered:


[[X5]]Introduction
------------------
Made for bioinformatics and life sciences, MOLGENIS compute is a flexible shell script 
framework to generate big data workflows that can run parallel on clusters and grids. 
Molgenis Compute Users can:

. Design a workflow.csv with each step as a shell script 'protocol';
. Generate and run jobs by iterating over parameters.csv and execute on a compute backend;
. (Optional) use standardized file and tool management for portable workflows.


Get Started
-----------
The latest distribution of Molgenis Compute is available 
at [[X1]]http://www.molgenis.org/wiki/ComputeStart[Molgenis Compute start page]. 
Unzip the archive and the command-line version is ready to use.  

  unzip molgenis-compute-core-0.0.1-SNAPSHOT-distribution.zip
  cd molgenis-compute-core-0.0.1-SNAPSHOT

Create workflow
~~~~~~~~~~~~~~~

Now, use can create your first workflow just by command

  sh molgenis_compute.sh --create myfirst_workflow

The directory containing workflow is created.

  cd myfirst_workflow
and you will see the typical Molgenis Compute workflow structure

  /protocols              #folder with bash script 'protocols'
  /protocols/step1.sh     #example of a protocol shell script
  /protocols/step2.sh     #example of a protocol shell script
  workflow.csv            #file listing steps and parameter flow
  defaults.csv            #default parameters for workflow.csv (optional)
  parameters.csv          #parameters you want to run analysis on

The similar structure should be created for every workflow. The example +workflow.csv+file has
the following content

  step,protocol,parameterMapping
  step1,protocols/step1.sh,in=user_input
  step2,protocols/step2.sh,wf=user_workflowName;date=user_creationDate;strings=step1_out

This means that the workflow consists of two steps 'step1' and 'step2'. All parameters, that 
specified in +parameters.csv+ is mapped in +workflow.csv+ using 'user_' prefix. Parameters, 
which are output of previous steps have prefix with a name of the step. In this example
the 'step1_' prefix means, that the 'out' variable is the output of the 'step1' step.

The +parameters.csv+ file contains some workflow parameters. In this example, one parameter
'input' has two values 'hello' and 'bye'

  input
  hello
  bye

More parameters can be specified using the next format

  parameter1, parameter2
  value11,    value21
  value12,    value22
  value13,    value23

Alternatively, parameters can be specified in the +.properties+ style. The parameters file also should have
the +.properties+extension.

  parameter1 = value11, value12, value13
  parameter2 = value21, value22, value23

Values for the workflow to iterate over can be passed as CSV file with parameter names in the header (a-zA-Z0-9, starting with a-zA-Z) and parameter values in each row. Use quotes to escape commas, e.g. "a,b".

Each value is one of the following:

. a string
. an expression, e.g. "${p}file${c}", to create derived parameters such as directory paths
. a series i..j (where i and j are two integers), meaning from i to j including i and j
. a list of ';' separated values (may contain templates)

You can combine multiple parameter files: the values will be 'natural joined' based on overlapping columns.

Example with two parameter files:

molgenis -w path/to/workflow -p f1.csv -p f2.csv
Example f1.csv (white space will be trimmed):

  p0,  p2
  x,   1
  y,   2
Example f2.csv (white space will be trimmed):

  p1,  p2,    p3,     p4
  v1,  1..2,  a;b,    file${p2}
Merged and expanded result for f1.csv + f2.csv:

  p0,  p1,  p2,   p3,   p4
  x,   v1,  1,    a,    file1 
  x,   v1,  1,    b,    file1
  y,   v1,  2,    a,    file2
  y,   v1,  2,    b,    file2

More complex parameter examples can combine values with template, as following:

  foo=    item1 , item2
  bar=    ${foo}, item3
  number= 123

Here, variable 'bar' has two values of variable 'foo'.

The example protocols has the following listings:

+step1.sh+

  #string in
  #output out
  # Let's do something with string 'in'
  out=${in}_hasBeenInStep1

+step2.sh+

  #string wf
  #string date
  #list strings
  echo "Workflow name: ${wf}"
  echo "Created: ${date}"
  echo "Result of step1.sh:"
  for s in "${strings[@]}"
  do
    echo ${s}
  done
  echo "(FOR TESTING PURPOSES: your runid is ${runid})"

In the header of protocols, we specify inputs with flags '#string' for variables with a single value 
and '#list' for variables with multiple values. 

I our example variables 'date' and 'wf' are defined in 
additional parameters file +workflow.defaults.csv+.

  workflowName,creationDate
  myFirstWorkflow,today

In this way, the parameters can be divided in several group and re-used in different workflows.

In this example, in +workflow.csv+ parameters are mapped to protocols using different names, for example:

  step1,protocols/step1.sh,in=user_input

However, they can have the same names

  step1,protocols/step1.sh,input=user_input

Then, +step1.sh+ will look like

  #string input
  #output out
  # Let's do something with string 'input'
  out=${input}_hasBeenInStep1


Generate workflow
~~~~~~~~~~~~~~~~~

To generate actual workflow jobs, run the next command-line

  sh molgenis_compute.sh --generate --parameters myfirst_workflow/parameters.csv --workflow myfirst_workflow/workflow.csv

The directory +rundir+ is created.

  ls rundir/

It contains a number of files

  doc		step1_0.sh	step1_1.sh	step2_0.sh	submit.sh	user.env

+.sh+ are actual scripts generated from the specified workflow. 'step1' has two scripts and 'step2' has only one, because it treats
outputs from scripts of the 'step1' as a list, which is specified in +step2.sh+by

    #list strings

+user.env+ contains all actual parameters mappings. In this example:

  #
  ## User parameters
  #
  user_creationDate[0]="today"
  user_creationDate[1]="today"
  user_input[0]="hello"
  user_input[1]="bye"
  user_workflowName[0]="myFirstWorkflow"
  user_workflowName[1]="myFirstWorkflow"

Execute workflow
~~~~~~~~~~~~~~~~

The workflow can be executed with the command

  sh molgenis_compute.sh --run
  ls rundir/

Now, +rundir+ contains more files

  doc				step1_0.sh.finished		step1_1.sh.finished		step2_0.sh.finished
  molgenis.bookkeeping.log	step1_0.sh.started		step1_1.sh.started		step2_0.sh.started
  step1_0.env			step1_1.env			step2_0.env			submit.sh
  step1_0.sh			step1_1.sh			step2_0.sh			user.env

+.started+ and +.finished+ files are created, when certain jobs is started and finished respectively. 

Command-line options
--------------------

The Molgenis Compute has the following command-line options:

  -b,--backend <arg>                 Backend for which you generate.
                                     Default: localhost
  -bp,--backendpassword <arg>        Supply user pass to login to execution
                                     backend. Default is not saved.
  -bu,--backenduser <arg>            Supply user name to login to execution
                                     backend. Default is your own user
                                     name.
  -clear,--clear                     Clear properties file
  -create <arg>                      Creates empty workflow. Default name:
                                     myworkflow
  -d,--database <arg>                Host, location of database. Default:
                                     none
  -dbe,--database-end                End the database
  -dbs,--database-start              Starts the database
  -defaults,--defaults <arg>         Path to your workflow-defaults file.
                                     Default: defaults.csv
  -footer <arg>                      Adds a custom footer. Default:
                                     footer.ftl
  -g,--generate                      Generate jobs
  -h,--help                          Shows this help.
  -header <arg>                      Adds a custom header. Default:
                                     header.ftl
  -l,--list                          List jobs, generated, queued, running,
                                     completed, failed
  -mpass,--molgenispassword <arg>    Supply user pass to login to molgenis.
                                     Default is not saved.
  -mu,--molgenisuser <arg>           Supply user name to login to molgenis
                                     database. Default is your own user
                                     name.
  -p,--parameters <parameters.csv>   Path to parameter.csv file(s).
                                     Default: parameters.csv
  -path,--path                       Path to directory this generates to.
                                     Default: <current dir>.
  -port,--port <arg>                 Port used to connect to databasae.
                                     Default: 8080
  -r,--run                           Run jobs from current directory on
                                     current backend. When using --database
                                     this will return a 'id' for --pilot.
  -rundir <arg>                      Directory where jobs are stored
  -runid,--runid <arg>               Id of the task set which you generate.
                                     Default: null
  -submit <arg>                      Set a custom submit.sh template.
                                     Default: submit.sh.ftl
  -w,--workflow <workflow.csv>       Path to your workflow file. Default:
                                     workflow.csv

Reserved words
--------------

Molgenis Compute has a list of reserved words, which cannot be used in compute to name 
parameters workflow steps etc. These words are listed below:

  port			interval
  workflow		path
  defaults		parameters
  rundir		runid
  backend		database
  walltime		nodes
  ppn			queue
  mem			_NA
  password		molgenisuser
  backenduser		header
  footer		submit
  autoid

The reserved word are used in compute.properties file. This file is created to save the latest
compute configuration and discuss further.

Advanced Compute Features
-------------------------

Database usage
--------------

Advanced A: Imputation workflow example
---------------------------------------

Advanced B: NGS Alignment workflow example
------------------------------------------

