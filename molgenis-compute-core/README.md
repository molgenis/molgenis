# Pilot for compute5 functions

## How to run

Checkout 'molgenis' and 'molgenis_compute5'

	git clone https://www.github.com/molgenis/molgenis.git
	git clone https://www.github.com/molgenis/molgenis_compute5.git
	
Import into Eclipse 

	File -> Import ... -> Existing projects into workspace (choose both)
	
Inside Eclipse create a run configuration for ComputeCommandline with arguments

Example 1: split merge

	-w src/main/resources/workflows/splitmerge/workflow.csv -p src/main/resources/workflows/splitmerge/parameters.csv -d src/main/resources/workflows/splitmerge/out

Example 2: generate a burn down chart

	-p src/main/resources/workflows/burnDownChart/parameters.csv
	-d /Users/mdijkstra/Documents/work/gitmaven/molgenis/molgenis-compute-core/src/main/resources/workflows/burnDownChart/out

The -p refers to your parameter csv, -w refers to your workflow (may also be specified in the parameter csv) and -d refers to the path where you want the generated files (scripts and documentation).

## How to make a parameter.csv file
Above, you have created a run configuration for our 'split merge' example. The '-p' parameter refers to your parameters csv file. This file describes your parameters and their values as follows. The first row contains the comma-separated parameter names, and the second row contains their subsequent comma-separated values. Each value is one of the following:

* a string (may contain Freemarker templates) or number
* a series i..j (where i and j are two integers), meaning from i to j including i and j
* a series of comma-separated numbers or strings (may contain Freemarker templates) "v1, v2" surrounded with quotes

The parameter csv file may contain a column 'parameters' with other parameter files you want to include as values. If you want to include more than one file then please use the following format: "file1, file2". If you want to include a file with a relative path, then the path of the current file is prepended. This enables you to move around sets of 'include files' without updating paths.

The 'parameters' parameter is the only exception that is not liable to expansion; see section "How parameter files are expanded", below.

You can specify the path to your workflow file in the parameters by adding a parameter 'workflow'. Again, if the path to the workflow file is relative, then the path of the current parameter file is prepended.

A parameters file should at least contain one parameter and a value for each parameter.

### How parameter files are expanded
The total number of rows that results from expanding a single row is the product of the lengths of all lists in that row. However, as mentioned before, the 'parameters' column is ignored in the process of expansion. 
	
	A parameters file:
	p1  p2    p3      parameters
	v1  1..2  "a,b"   "f3,f4"

	After expansion:
	p1  p2   p3    parameters
	v1  1    a     "f3,f4"
	v1  1    b     "f3,f4"
	v1  2    a     "f3,f4"
	v1  2    b     "f3,f4"


### Multi-row parameter.csv files
Next to the header with parameter names, a parameter files may contain more than one row with values. The values on each given row, may be used to substitute the corresponding parameters in a given protocol (template), resulting in a script corresponding to that row. Each different row may thus generate a different script.

In general, a parameter may take different values on different rows, except for the 'parameters' parameter, which refers to parameter files that are included. The value of the 'parameters' parameter should be the same on every row.

If two rows contain a different value for the 'workflow' parameter, then this means that these rows will be processed by different workflows.

### Merging parameter files
Before merging, each parameter file is first expanded. If a parameter file (say f1) includes another one (say f2), then the two files will be merged as follows. Let P1 and P2 be the set of parameters in f1 and f2, respectively. The parameter set in the merged file is the union of P1 and P2 parameters, i.e. P1 &#8746; P2. Let P = P1 &#8745; P2 \ {'parameters'} be the set of parameters that is shared by f1 and f2, excluding the 'parameters' parameter. The values for P on each row i in f1 are compared to the values for P on each row j in f2, for each i and j. If the values match, then all values on row i in f1 and row j f2 are combined into a new row in the merged parameters file. The value of the 'parameters' parameter on that row is the union of the 'parameters' values in f1 and f2.

We do not allow 'missing values', so the set of values for P in f1 must be equal to the set of values for P in f2, otherwise you'll get an error.

#### Example:
	File f1:
	p0  p2   parameters
	x   1    f2
	y   2    f2

	File f2:
	p1  p2    p3      parameters
	v1  1..2  "a,b"   "f3,f4"

	become, after first expanding f1 and f2, and then merging:

	p0  p1  p2   p3    parameters
	x   v1  1    a     "f2,f3,f4"
	x   v1  1    b     "f2,f3,f4"
	y   v1  2    a     "f2,f3,f4"
	y   v1  2    b     "f2,f3,f4"


## To do's

* Make workflow 'template-able'
* Allow steps without parameters? Currently, in workflow the parameters column may not be empty.
* Enable transposed parameter files
* If two rows contain a different value for the 'workflow' parameter, then this means that these rows will be processed by different workflows.
* If a row contains a list of values for the 'workflow' parameter, then this row will be processed in parallel by each of the workflows.
	
## Known limitations

* Workflow behavior cannot depend on results from previous steps. Examples: based on counts I get a limitted set of chunks.