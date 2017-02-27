# MOLGENIS/compute

**
In this quickstart you will learn to create a workflow, create a workflow run and execute the run.
**

# 1. Create a workflow

We assume you   downloaded and unzipped molgenis compute commandline and are now in the directory you downloaded.

You can generate a template for a new workflow using command:

```bash
  sh molgenis_compute.sh --create myfirst_workflow
```

This will create a new directory for the workflow:

```bash
  cd myfirst_workflow
  ls
```

The directory contains a typical Molgenis Compute workflow structure

```bash
  /protocols              #folder with bash script 'protocols'
  /protocols/step1.sh     #example of a protocol shell script
  /protocols/step2.sh     #example of a protocol shell script
  workflow.csv            #file listing steps and parameter flow
  workflow.defaults.csv   #default parameters for workflow.csv (optional)
  parameters.csv          #parameters you want to run analysis on
  header.ftl              #user extra script header (optional)
  footer.ftl              #user extra script footer (optional)
```
## Define workflow

You can define a workflow of steps using the workflow.csv file. 
For example:

```
  step,protocol,dependencies
  step1,protocols/step1.sh,
  step2,protocols/step2.sh,step1
```

This example consists of two steps 'step1' and 'step2', where 'step2' depends on 'step1'. 'step1' has its contents in the file protocols/step1.sh and 'step2' in the file protocols/step2.sh respectively.

If we want parameter values to flow between steps, we can also map the parameters:
```
  step,protocol,parameterMapping
  step1,protocols/step1.sh,in=input
  step2,protocols/step2.sh,wf=workflowName;date=creationDate;strings=step1.out;in=input
```

## Define parameters

To feed parameter values to your workflow you can also use simple csv files. In this example, one parameter 'input' has two values 'hello' and 'bye':

```
  input
  hello
  bye
```

## Define step contents

Finally, you need to implement what needs to happen at each step. We therefor define for each step a 'protocol'. Protocols are simply bash scripts containing the commands you want to run

For example protocols/step1.sh:
```bash
  #string in
  #output out
  echo ${in}_hasBeenInStep1
  out=${in}_hasBeenInStep1
```

Given the parameters above, 'input' will be substituted with values 'hello' or 'bye'.
In addition, the contents of 'out' will be available to the next step.

Inputs can either be '#string' for variables with a single value or '#list' for variables with multiple values. The outputs are specified with the flag '#output'

In the same way, we can map outputs of one step to the inputs of the next steps. In our example, 'strings' in the 'step2', which has protocol step2.ftl

```bash
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
```

The example protocols has the following listings:

In our example variables 'date' and 'wf' are defined in 
an additional parameters file +workflow.defaults.csv+.

```
  workflowName,creationDate
  myFirstWorkflow,today
```

In this way, the parameters can be divided in several groups and re-used in different workflows. If users do not like to map 
parameters, they should use the same names in protocols and parameters files. This makes parameters a kind of global.

# 2. Generate jobs

Once you defined your workflow you can generate 1000s of jobs. Just change the parameter values to have different runs. 

```bash
  sh molgenis_compute.sh --generate --parameters myfirst_workflow/parameters.csv --workflow myfirst_workflow/workflow.csv --defaults myfirst_workflow/workflow.defaults.csv
```

or with a short command-line version

```bash
  sh molgenis_compute.sh -g -p myfirst_workflow/parameters.csv -w myfirst_workflow/workflow.csv -defaults myfirst_workflow/workflow.defaults.csv
```

The directory `rundir` is created.

```bash
  ls rundir/
```

It contains a number of files

```bash
  doc        
  step1_0.sh    
  step1_1.sh    
  step2_0.sh    
  submit.sh    
  user.env
```

.sh are actual scripts generated from the specified workflow. 'step1' has two scripts and 'step2' has only one, because it treats
outputs from scripts of the 'step1' as a list, which is specified in step2.sh by

```bash
    #list strings
```
user.env contains all actual parameters mappings. In this example:

```bash
  #
  ## User parameters
  #
  creationDate[0]="today"
  creationDate[1]="today"
  input[0]="hello"
  input[1]="bye"
  workflowName[0]="myFirstWorkflow"
  workflowName[1]="myFirstWorkflow"
```

Parameters, which are known before hand can be connected to the environment file or weaved directly in the protocols (if 'weave' flag is set in command-line options). In our example, two shell scripts are generated for 
the 'step1'. The weaved version of generated files are shown below.

step1_0.sh:
```bash
  #string in
  #output out
  # Let's do something with string 'in'
  echo "hello_hasBeenInStep1"
  out=hello_hasBeenInStep1
```

and step1_1.sh
```bash
  #string in
  #output out
  # Let's do something with string 'in'
  echo "bye_hasBeenInStep1"
  out=bye_hasBeenInStep1
```

The output values of the first steps are not known beforehand, so, 'string' cannot be weaved and will stay in the generated for the 'step2' script as it was. However, the 'wf' and 'date' values are weaved.

step2_0.sh:
```bash
  #string wf
  #string date
  #list strings
  echo "Workflow name: myFirstWorkflow"
  echo "Created: today"
  echo "Result of step1.sh:"
  for s in "${strings[@]}"
  do
      echo ${s}
  done
```

If values can be known, the script will have the following content 

step2_0.sh with all known values:
```bash
  #string wf
  #string date
  #list strings
  echo "Workflow name: myFirstWorkflow"
  echo "Created: today"
  echo "Result of step1.sh:"
  for s in "hello" "bye"
  do
      echo ${s}
  done
```

If 'weaved' flag is not set, +step1_0.sh+ file, for example looks as follows:
```bash
  # Connect parameters to environment
  input="bye"
  #string input
  # Let's do something with string 'in'
  echo "${input}_hasBeenInStep1"
  out=${input}_hasBeenInStep1
```

In this way, users can choose how generated files look like.
In the current implementation, values are first taken from parameter files. If they are not present, then compute looks,
if these values can be known at run-time, by analysing all previous steps of the protocol, where values are unknown.
If values cannot be known at run-time, compute will give a generation error. 

# 3. Execute workflow

## Execute locally
Compute can execute the jobs locally with command:

```bash
  sh molgenis_compute.sh --run
  ls rundir/
```

Now, rundir contains more files
```bash
  doc                
  step1_0.sh            
  step1_1.sh            
  step2_0.sh
  submit.sh
  step1_0.sh.started        
  step1_1.sh.started        
  step2_0.sh.started
  step1_0.env            
  step1_1.env            
  step2_0.env    
  step1_0.sh.finished        
  step1_1.sh.finished        
  step2_0.sh.finished
  molgenis.bookkeeping.log        
  user.env
```
.started and .finished files are created, when certain jobs are started and finished respectively. 

In our example, 'strings' variable from 'step2' requires run-time values produced in 'step1'. These values are taken from step1_X.env files. For example:

step1_0.env:
```bash
  step1__has__out[0]=hello_hasBeenInStep1
```

In the workflow.csv file, it is specified with a simple '.' 
```bash
  strings=step1.out
```

and substituted with '__has__' in generated script files.